package tr.com.serkanozal.jillegal.service;

/**
 * @author SERKAN OZAL
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.agent.JillegalAgent;

@SuppressWarnings( "unchecked" )
public class JillegalMemoryService
{
    public static final byte SIZE_32_BIT                    = 4;
    public static final byte SIZE_64_BIT                    = 8;
    public static final byte INVALID_ADDRESS                = -1;
    /**
     * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/oop.hpp
     */
    public static final long CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_32_BIT = 4L;
    public static final long CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_64_BIT = 8L;
    /**
     * @link http://hg.openjdk.java.net/jdk7/hotspot/hotspot/file/9b0ca45cd756/src/share/vm/oops/klass.hpp
     */
    public static final long SIZE_FIELD_OFFSET_IN_CLASS     = 12L;
    
    public static final long CLASS_HEADER_SIZE 				= 8;
    private static final int NR_BITS 						= Integer.valueOf( System.getProperty( "sun.arch.data.model" ) );
    private static final int BYTE 							= 8;
    private static final int WORD 							= NR_BITS / BYTE;
    private static final int MIN_SIZE 						= 16; 
    
    private static Unsafe   unsafe;
    private static Object[] objArray;
    private static long     baseOffset;
    private static int      addressSize;
    private static int      indexScale;
    
    static
    {
        init( );
    }
    
    private JillegalMemoryService( )
    {
        throw new 
            UnsupportedOperationException
            ( 
                 "Can't create instance of " + "<" + getClass( ).getName( ) + ">" 
            );
    }
    
    public static void init( )
    {
        initUnsafe( );
        objArray    = new Object[1];
        baseOffset  = unsafe.arrayBaseOffset( Object[].class );
        indexScale  = unsafe.arrayIndexScale( Object[].class );
        addressSize = unsafe.addressSize( );
    }
    
    private static void initUnsafe( )
    {
        try
        {
            Field field = Unsafe.class.getDeclaredField( "theUnsafe" );
            field.setAccessible( true );
            unsafe = ( Unsafe ) field.get( null );
        }
        catch ( Exception e )
        {
            e.printStackTrace( );
        }
    }
    
    public static Unsafe getUnsafe( ) 
    {
        return unsafe;
    }
    
    public static void info( )
    {
        System.out.println( "Unsafe: " + unsafe );
        System.out.println( "\tAddressSize : " + unsafe.addressSize( )  );
        System.out.println( "\tPage Size   : " + unsafe.pageSize( )     );
    }
    
    public static long normalize( int value ) 
    {
        if ( value >= 0 ) 
            return value;
        else
            return ( ~0L >>> 32 ) & value;
    }
    
    public static long sizeOfWithAgent( Object obj ) 
    {
        return JillegalAgent.sizeOf( obj );
        /*
        if ( obj == null )
            return 0;
        else
        {
            switch ( addressSize )
            {
                case SIZE_32_BIT:
                    return
                            unsafe.getAddress
                            ( 
                                 normalize
                                 ( 
                                      unsafe.getInt( obj, CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_32_BIT ) 
                                 ) + 
                                 SIZE_FIELD_OFFSET_IN_CLASS 
                            );  
                    
                case SIZE_64_BIT:
                    return
                            unsafe.getAddress
                            ( 
                                 normalize
                                 ( 
                                      unsafe.getInt( obj, CLASS_FIELD_OFFSET_IN_INSTANCE_FOR_64_BIT ) 
                                 ) + 
                                 SIZE_FIELD_OFFSET_IN_CLASS
                            );  
                    
                default:
                    throw new AssertionError( "Unsupported address size: " + addressSize );    
            }
        }    
        */
    }  
    
    public static long sizeOfWithReflection( Class<?> objClass ) 
    {
    	List<Field> instanceFields = new LinkedList<Field>( );
    	
        do
        {
            if ( objClass == Object.class ) 
            	return MIN_SIZE;
            
            for ( Field f : objClass.getDeclaredFields( ) ) 
            {
                if ( ( f.getModifiers( ) & Modifier.STATIC ) == 0 )
                    instanceFields.add( f );
            }
            
            objClass = objClass.getSuperclass( );
        } 
        while ( instanceFields.isEmpty( ) );

        long maxOffset = 0;
        for ( Field f : instanceFields ) 
        {
            long offset = unsafe.objectFieldOffset( f );
            if ( offset > maxOffset ) 
            	maxOffset = offset; 
        }
        return ( ( ( long ) maxOffset / WORD ) + 1 ) * WORD; 
    }	

    public static long internalAddressOf( Object obj ) 
    {
        return normalize( System.identityHashCode( obj ) );
    }
    
    public static String toHexAddress( long address )
    {
        return "0x" + Long.toHexString( address ).toUpperCase( );
    }
    
    public static long addressOf( Object obj )
    {
        if ( obj == null )
            return 0;
        
        objArray[0]         = obj;
        long objectAddress  = INVALID_ADDRESS;

        switch ( indexScale )
        {
            case SIZE_32_BIT:
            {
                switch ( addressSize ) 
                {
                    case SIZE_32_BIT:
                        objectAddress = unsafe.getInt( objArray, baseOffset );
                        break;
                        
                    case SIZE_64_BIT:
                        objectAddress = unsafe.getLong( objArray, baseOffset );
                        break;    
                }
                break;
            }
            
            case SIZE_64_BIT:
                throw new AssertionError( "Unsupported index scale: " + indexScale );
                    
            default:
                throw new AssertionError( "Unsupported index scale: " + indexScale );
        }       

        return objectAddress;
    }
  
    public static long addressOfField( Object obj, String fieldName ) 
        throws SecurityException, NoSuchFieldException
    {
        long    objectAddress   = addressOf( obj );
        Field   field           = obj.getClass( ).getDeclaredField( fieldName );
        long    fieldOffset     = unsafe.objectFieldOffset( field );
        
        return objectAddress + fieldOffset;
    }
    
    public static <T> T getObject( long address )
    {
        switch ( addressSize )
        {
            case SIZE_32_BIT:
                unsafe.putInt( objArray, baseOffset, ( int ) address );
                break;
                
            case SIZE_64_BIT:
            	System.out.println(address);
                unsafe.putLong( objArray, baseOffset, address );
                break;    
                
            default:
                throw new AssertionError( "Unsupported index scale: " + indexScale );
        }        
        return ( T ) objArray[0];
    }
    
    public static <T> void setObject( long address, T obj )
    {
        if ( obj == null )
        {
            switch ( addressSize )
            {
                case SIZE_32_BIT:
                    unsafe.putInt( address, 0 );
                    break;
                    
                case SIZE_64_BIT:
                    unsafe.putLong( address, 0L );
                    break;    
                    
                default:
                    throw new AssertionError( "Unsupported address size: " + addressSize );
            }
        }
        else 
        {
            long objSize    = sizeOfWithAgent( obj );
            long objAddress = addressOf( obj );
            unsafe.copyMemory( objAddress, address, objSize );   
        }    
    }
    
    public static <T> T changeObject( T source, T target )
    {
        if ( source == null )
            throw new IllegalArgumentException( "Source object is null !" );
        
        long sourceAddress = addressOf( source );
        setObject( sourceAddress, target );
        
        return target;
    }
    
    public static long allocateMemory( long size )
    {
    	return unsafe.allocateMemory( size );
    }
    
}
