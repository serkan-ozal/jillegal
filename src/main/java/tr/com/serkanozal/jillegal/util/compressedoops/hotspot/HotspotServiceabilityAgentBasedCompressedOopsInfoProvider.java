/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.runtime.VM;
import sun.management.VMManagement;
import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.ClasspathUtil;
import tr.com.serkanozal.jillegal.util.compressedoops.CompressedOopsInfo;

@SuppressWarnings( { "restriction" } )
public class HotspotServiceabilityAgentBasedCompressedOopsInfoProvider implements HotspotCompressedOopsInfoProvider {

	private static final Logger logger = 
			Logger.getLogger(HotspotServiceabilityAgentBasedCompressedOopsInfoProvider.class);
	
	private static final String SYSTEM_CLASSLOADER_VM_ARGUMENT_NAME = "java.system.class.loader";
	
	private static final String JVM_INFO_RETRIEVE_START = "$$$_JVM_INFO_RETRIEVE_START_$$$";
	private static final String JVM_INFO_RETRIEVE_FINISH = "$$$_JVM_INFO_RETRIEVE_FINISH_$$$";
	private static final String JVM_INFO_LINE = "$$$_JVM_INFO_LINE_$$$";
	private static final String NARROW_OOP_BASE_INFO_KEY = "NARROW_OOP_BASE_INFO";
	private static final String NARROW_OOP_SHIFT_INFO_KEY = "NARROW_OOP_SHIFT_INFO";
	private static final String NARROW_KLASS_BASE_INFO_KEY = "NARROW_KLASS_BASE_INFO";
	private static final String NARROW_KLASS_SHIFT_INFO_KEY = "NARROW_KLASS_SHIFT_INFO";
	
	@Override
	public CompressedOopsInfo getCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef) {
		if (isHotspotJvm()) {
			return findCompressedOopsInfo(findProcessId());
		}
		else {
			return null;
		}
	}
	
	private boolean isHotspotJvm() {
		String name =  System.getProperty("java.vm.name").toLowerCase();
		return name.contains("hotspot") || name.contains("openjdk");
	}
	
	private CompressedOopsInfo findCompressedOopsInfo(int processId) {
		if (processId == 0) {
			return null;
		}
		try {
			long narrowOopBase = -1;
			int narrowOopShift = -1;
			long narrowKlassBase = 0;
			int narrowKlassShift = 0;
			StringBuilder cpBuilder = new StringBuilder();
			Set<URL> cpUrls = ClasspathUtil.getClasspathUrls();
			for (URL cpUrl : cpUrls) {
				String cpUrlPath = cpUrl.getPath();
				if (cpUrlPath.contains("sa_jdi")) {
					continue;
				}	
				cpBuilder.append(cpUrlPath).append(File.pathSeparator);
			}
			String classpath = cpBuilder.toString().replace("%20", " ");
			
			List<String> args = new ArrayList<String>();
			args.add(System.getProperty("java.home") + "/" + "bin" + "/" + "java");
			args.add("-cp");
			args.add(classpath);
			args.add("-D" + SYSTEM_CLASSLOADER_VM_ARGUMENT_NAME + "=" + HotspotJvmAwareSaJdiClassLoader.class.getName());
			args.add("-D" + HotspotJvmAwareSaJdiClassLoader.URL_CLASSPATH_VM_ARGUMENT_NAME + "=" + cpUrls);
			args.add(HotspotServiceabilityAgentBasedCompressedOopsInfoProvider.class.getName());
			args.add(String.valueOf(processId));

			Process p =  new ProcessBuilder(args).start();
	        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        for (String line = br.readLine(); line != null; line = br.readLine()) {
	        	// System.out.println(line);
	        	String[] parts = line.split("[\\s]+");
	        	if (JVM_INFO_LINE.equals(parts[0])) {
	        		if (NARROW_OOP_BASE_INFO_KEY.equals(parts[1])) {
	        			narrowOopBase = Long.parseLong(parts[2]);
	        		}
	        		else if (NARROW_OOP_SHIFT_INFO_KEY.equals(parts[1])) {
	        			narrowOopShift = Integer.parseInt(parts[2]);
	        		}
	        		else if (NARROW_KLASS_BASE_INFO_KEY.equals(parts[1])) {
	        			narrowKlassBase = Long.parseLong(parts[2]);
	        		}
	        		else if (NARROW_KLASS_SHIFT_INFO_KEY.equals(parts[1])) {
	        			narrowKlassShift = Integer.parseInt(parts[2]);
	        		}
	        	}
	        	else if (JVM_INFO_RETRIEVE_FINISH.equals(line.trim())) {
	        		break;
	        	}
	        }
	        BufferedReader brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        for (String line = brErr.readLine(); line != null; line = brErr.readLine()) {
	        	System.err.println(line);
	        }
	        p.destroy();
	        
	        System.out.println("Narrow oop base    : " + narrowOopBase);
	        System.out.println("Narrow oop shift   : " + narrowOopShift);
	        System.out.println("Narrow klass base  : " + narrowKlassBase);
	        System.out.println("Narrow klass shift : " + narrowKlassShift);
	        
	        if (narrowOopBase != -1 && narrowOopShift != -1) {
	        	logger.info("Narrow oop base    : " + narrowOopBase);
	        	logger.info("Narrow oop shift   : " + narrowOopShift);
	        	logger.info("Narrow klass base  : " + narrowKlassBase);
	        	logger.info("Narrow klass shift : " + narrowKlassShift);
	        	return new CompressedOopsInfo(narrowOopBase, narrowOopShift, narrowKlassBase, narrowKlassShift);
	        }
	        else {
	        	return null;
	        }
		} 
		catch (Throwable t) {
			logger.error("Unable to find JVM Info", t);
			return null;
		}
	}
	
	private int findProcessId() {
		try {
	   	 	RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
	        Field jvmField = mxbean.getClass().getDeclaredField("jvm");
	
	        jvmField.setAccessible(true);
	        VMManagement management = (VMManagement) jvmField.get(mxbean);
	        Method method = management.getClass().getDeclaredMethod("getProcessId");
	        method.setAccessible(true);
	        return (Integer) method.invoke(management);
		}
		catch (Throwable t) {
			logger.error("Unable to find current process id", t);
			return 0;
		} 
	}	
	
	public static void main(final String[] args) throws InterruptedException {
		try {
			System.out.println(JVM_INFO_RETRIEVE_START);
			
			System.setProperty("sun.jvm.hotspot.debugger.useProcDebugger", "true");
			System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");
			System.setProperty("sun.jvm.hotspot.runtime.VM.disableVersionCheck", "true");
			
			final HotSpotAgent agent = new HotSpotAgent();
			Thread t = new Thread() {
				public void run() {
					agent.attach(Integer.parseInt(args[0])); 
				};
			};
			t.start();
			
			boolean vmInitialized = false;
			// Check five times :)
			for (int i = 0; i < 5; i++) {
				Thread.sleep(1000);
				try {
					if (VM.getVM() != null) {
						vmInitialized = true;
						break;
					}
				}
				catch (Throwable err) {
					
				}
			}
			
			if (vmInitialized) {
				try {
					System.out.println(JVM_INFO_LINE + " " + NARROW_OOP_BASE_INFO_KEY + " " + Universe.getNarrowOopBase());
					System.out.println(JVM_INFO_LINE + " " + NARROW_OOP_SHIFT_INFO_KEY + " " + Universe.getNarrowOopShift());
					System.out.println(JVM_INFO_LINE + " " + NARROW_KLASS_BASE_INFO_KEY + " " + Universe.getNarrowKlassBase());
					System.out.println(JVM_INFO_LINE + " " + NARROW_KLASS_SHIFT_INFO_KEY + " " + Universe.getNarrowKlassShift());
				}
				catch (Throwable e) {
					e.printStackTrace(System.out);
				}
			}
			else {
				System.out.println("VM couldn't be initialized !");
			}
			
			agent.detach();
			
			System.out.println(JVM_INFO_RETRIEVE_FINISH);
		}
		catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	
}
