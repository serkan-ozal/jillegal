/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.runtime.VM;
import sun.management.VMManagement;

@SuppressWarnings("restriction")
public class HotspotJvmInfoUtil {

	private static final Logger logger = Logger.getLogger(HotspotJvmInfoUtil.class);
	
	private static HotspotJvmInfo jvmInfo;
	
	private static final String JVM_INFO_RETRIEVE_START = "$$$_JVM_INFO_RETRIEVE_START_$$$";
	private static final String JVM_INFO_RETRIEVE_FINISH = "$$$_JVM_INFO_RETRIEVE_FINISH_$$$";
	private static final String JVM_INFO_LINE = "$$$_JVM_INFO_LINE_$$$";
	private static final String NARROW_OOP_BASE_INFO_KEY = "NARROW_OOP_BASE_INFO";
	private static final String NARROW_OOP_SHIFT_INFO_KEY = "NARROW_OOP_SHIFT_INFO";
	
	private HotspotJvmInfoUtil() {
        
	}
	
	public static void main(final String[] args) throws InterruptedException {
		try {
			System.out.println(JVM_INFO_RETRIEVE_START);
			
			System.setProperty("sun.jvm.hotspot.debugger.useProcDebugger", "true");
			System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");
			
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

	public static HotspotJvmInfo getJvmInfo() {
		if (jvmInfo == null) {
			jvmInfo = findJvmInfo(findProcessId());
		}
		return jvmInfo;
	}
	
	private static HotspotJvmInfo findJvmInfo(int processId) {
		if (processId == 0) {
			return null;
		}
		try {
			long narrowOopBase = -1;
			int narrowOopShift = -1;
			
			List<String> args = new ArrayList<String>();
			args.add(System.getProperty("java.home") + "/" + "bin" + "/" + "java");
			args.add("-cp");
			args.add(ClasspathUtil.getFullClasspath());
			args.add(HotspotJvmInfoUtil.class.getName());
			args.add(String.valueOf(processId));

			Process p =  new ProcessBuilder(args).start();
	        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        for (String line = br.readLine(); line != null; line = br.readLine()) {
	        	String[] parts = line.split("[\\s]+");
	        	if (JVM_INFO_LINE.equals(parts[0])) {
	        		if (NARROW_OOP_BASE_INFO_KEY.equals(parts[1])) {
	        			narrowOopBase = Long.parseLong(parts[2]);
	        		}
	        		else if (NARROW_OOP_SHIFT_INFO_KEY.equals(parts[1])) {
	        			narrowOopShift = Integer.parseInt(parts[2]);
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
	        
	        System.out.println("Narrow oop base  : " + narrowOopBase);
	        System.out.println("Narrow oop shift : " + narrowOopShift);
	        
	        if (narrowOopBase != -1 && narrowOopShift != -1) {
	        	logger.info("Narrow oop base  : " + narrowOopBase);
	        	logger.info("Narrow oop shift : " + narrowOopShift);
	        	return new HotspotJvmInfo(narrowOopBase, narrowOopShift);
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
	
	private static int findProcessId() {
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
	
	public static class HotspotJvmInfo {
		
		public final long narrowOopBase;
		public final int narrowOopShift;
		
		public HotspotJvmInfo(long narrowOopBase, int narrowOopShift) {
			this.narrowOopBase = narrowOopBase;
			this.narrowOopShift = narrowOopShift;
		}
		
	}
	
}
