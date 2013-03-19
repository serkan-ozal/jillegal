/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.agent;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import tr.com.serkanozal.jillegal.util.JillegalUtil;

import com.sun.tools.attach.VirtualMachine;

public class JillegalAgent {   
	
	private final static Logger logger = Logger.getLogger(JillegalAgent.class);
	
    final static public String CLASS_PATH = System.getProperty("java.class.path");
	final static public String INSTR_JAR_NAME = "jillegal.jar";
	final static public String OS_NAME = System.getProperty("os.name");
	
	private static Instrumentation inst;
	private static boolean agentLoaded = false;
	
	private JillegalAgent() {
	    
	}

	public static void agentmain(String arguments, Instrumentation i) {   
	    initAtMain(arguments, i);
	    logger.debug("agentmain = " + inst + " - " + "arguments = " + arguments);
	}
	
    public static void premain(String arguments, Instrumentation i) {
        initAtMain(arguments, i);
        logger.debug("remain = " + inst + " - " + "arguments = " + arguments);
    }
    
    private static void initAtMain(String arguments, Instrumentation i) {
        try {
            inst = i;
           
            JarFile agentJarFile = null;
            
            final StringTokenizer st = new StringTokenizer(CLASS_PATH, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String classpathEntry = st.nextToken().trim();
                if (classpathEntry.endsWith(INSTR_JAR_NAME)) {
                    agentJarFile = new JarFile(classpathEntry);
                    break;
                }
            }
            
            if (agentJarFile != null) {
                inst.appendToBootstrapClassLoaderSearch(agentJarFile);
            }    
            if (agentJarFile != null) {
                inst.appendToSystemClassLoaderSearch(agentJarFile);
            }    
        }
        catch (Throwable t) {
        	logger.error("Error at initAtMain", t);
        }
    }

    public static Instrumentation getInstrumentation() {
        return inst;
    }
    
    public static void init() {
        try {
            loadAgent();
        }
        catch (Throwable t) {
        	logger.error("Error at init", t);
        }
    }
  
    public static void loadAgent() throws Exception {
        loadAgent(null);
    }
    
	public static void loadAgent( String arguments ) throws Exception {
    	if (agentLoaded) {
    		return;
    	}
    	VirtualMachine vm = VirtualMachine.attach(JillegalUtil.getPidFromRuntimeMBean());
    	String agentPath = null;
    	logger.debug("Class Path = " + CLASS_PATH);
    	logger.debug("OS_NAME = " + OS_NAME );
    	
    	String classPathToUse = CLASS_PATH;
      
    	if (System.getProperty("surefire.test.class.path") != null) {
    		classPathToUse = System.getProperty("surefire.test.class.path");
    	}
    	logger.debug("Using ClassPath = " + classPathToUse);

    	for (String entry : classPathToUse.split(File.pathSeparator)) {
    		if (entry.endsWith(INSTR_JAR_NAME)) {
    			agentPath = entry;
    			break;
    		}
    	}
    	
    	if (agentPath == null) {
    		throw new RuntimeException("Profiler agent is not in classpath ...");
    	}
    	
    	if (arguments != null) {
    	    vm.loadAgent(agentPath, arguments);
    	}    
    	else {
    	    vm.loadAgent(agentPath);
    	}
    	vm.detach();

    	agentLoaded = true;
    }

    public static void redefineClass(Class<?> cls, byte[] byteCodes) {
        try {
            inst.redefineClasses(new ClassDefinition(cls, byteCodes));
        }
        catch (UnmodifiableClassException e) {
        	logger.error("Error at redefineClass", e);
        }
        catch (ClassNotFoundException e) {
        	logger.error("Error at redefineClass", e);
        }
    }
    
    public static long sizeOf(Object obj) {
        if (obj == null) {
            return 0;
        }    
        else {
            return inst.getObjectSize(obj);
        }    
    }
    
}
