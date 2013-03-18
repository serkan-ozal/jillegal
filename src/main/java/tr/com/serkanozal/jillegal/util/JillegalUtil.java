package tr.com.serkanozal.jillegal.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sun.management.VMManagement;

public class JillegalUtil
{
    private JillegalUtil( )
    {
        
    }
    
    public static String getPidFromRuntimeMBean( ) throws Exception 
    {
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean( );
        Field jvmField = mxbean.getClass().getDeclaredField( "jvm" );

        jvmField.setAccessible( true );
        VMManagement management = ( VMManagement ) jvmField.get( mxbean );
        Method method = management.getClass( ).getDeclaredMethod( "getProcessId" );
        method.setAccessible( true );
        Integer processId = ( Integer ) method.invoke( management );

        return processId.toString( );
    }
}
