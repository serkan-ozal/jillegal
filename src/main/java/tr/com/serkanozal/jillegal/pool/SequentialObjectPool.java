package tr.com.serkanozal.jillegal.pool;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.service.JillegalMemoryService;

public class SequentialObjectPool<T> {

	private Class<T> 	clazz;
	private long 		objectCount;
	private long 		objectSize;
	private long		currentMemoryIndex;
	private long		allocatedAddress;
	private T			sampleObject;
	private long		sampleObjectAddress;
	private long		addressLimit;
	private Unsafe		unsafe;
	
	@SuppressWarnings("unchecked")
	public SequentialObjectPool( Class<T> clazz, long objectCount ) 
	{
		this.clazz 				= clazz;
		this.objectCount 		= objectCount;
		this.objectSize 		= JillegalMemoryService.sizeOfWithReflection( clazz );
		this.allocatedAddress 	= JillegalMemoryService.allocateMemory( objectSize * objectCount );
		this.currentMemoryIndex	= allocatedAddress - objectSize;
		this.addressLimit		= allocatedAddress + ( objectCount * objectSize ) - objectSize;
		this.unsafe				= JillegalMemoryService.getUnsafe( );
	
		try 
		{
			this.sampleObject 			= clazz.newInstance(); //( T ) JillegalMemoryService.getUnsafe( ).allocateInstance( clazz );
			this.sampleObjectAddress 	= JillegalMemoryService.addressOf( sampleObject );
			T o = JillegalMemoryService.getObject(sampleObjectAddress);
			System.out.println(o.toString());
			System.out.println(sampleObjectAddress + ", " + allocatedAddress);
			for ( long l = 0; l < objectCount; l++ ) 
			{
				unsafe.copyMemory
				( 
					sampleObjectAddress, 
					allocatedAddress + ( l * objectSize ), 
					JillegalMemoryService.CLASS_HEADER_SIZE 
				);
			}
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Class<T> getClazz( ) 
	{
		return clazz;
	}
	
	public long getObjectCount( )  
	{
		return objectCount;
	}
	
	public T newObject( ) 
	{
		if ( currentMemoryIndex >= addressLimit ) {
			System.out.println("asasasa");
			return null;
		}
		return JillegalMemoryService.getObject( currentMemoryIndex += objectSize );
	}
	
	public T getObject( long objectIndex ) 
	{
		if ( objectIndex < 0 || objectIndex > objectCount )
			return null;
		return JillegalMemoryService.getObject( allocatedAddress + ( objectIndex * objectSize ) );
	}
	
	public void reset( )
	{
		currentMemoryIndex = allocatedAddress;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class SampleClass
    {
	
		private int i1 = 5;
		private int i2 = 10;
		private int order;
		
		public int getI1( ) 
		{
			return i1;
		}
		
		public int getI2( ) 
		{
			return i2;
		}
		
		public int getOrder( ) 
		{
			return order;
		}
		
		public void setOrder( int order ) {
			this.order = order;
		}
		
	}
	
	public static void main( String args[] ) 
    {
		final int OBJECT_COUNT = 10000;
		
		SequentialObjectPool<SampleClass> sequentialObjectPool = 
    			new SequentialObjectPool<SampleClass>( SampleClass.class, OBJECT_COUNT );
    	long start = System.currentTimeMillis( );
    	for ( int i = 0; i < OBJECT_COUNT; i++ ) 
    	{
    		SampleClass obj = sequentialObjectPool.newObject( );
    		obj.setOrder( i );
    	}
    	for ( int i = 0; i < OBJECT_COUNT; i++ ) 
    	{
    		SampleClass obj = sequentialObjectPool.getObject( i );
    		System.out.println( obj.getOrder( ) );
    	}
    	long finish = System.currentTimeMillis();
    	System.out.println(finish - start + " milliseconds ...");
    }
	
}
