/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

import junit.framework.Assert;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class DirectMemoryServiceTest {

	private DirectMemoryService directMemoryService = DirectMemoryServiceFactory.getDirectMemoryService();

	public static class SampleClass {
		
		private final static byte b = 100;
		
		private int i = 5;
		private long l = 10;
		
		public int getI() {
			return i;
		}
		
		public void setI(int i) {
			this.i = i;
		}

		public long getL() {
			return l;
		}
		
		public void setL(long l) {
			this.l = l;
		}

		public static byte getB() {
			return b;
		}
		
	}
	
	/*
	 * JFreeMemoryService.info( );
        
        Unsafe      unsafe          = JFreeMemoryService.getUnsafe( );
        MyObject    obj1            = new MyObject( );
        MyObject    obj2            = new MyObject( );
        MyObject    obj3            = new MyObject( );
        String      fieldName       = "i";
        long        objSize1        = JFreeMemoryService.sizeOf( obj1 );
        long        objAddress1     = JFreeMemoryService.addressOf( obj1 );
        long        fieldAddress1   = JFreeMemoryService.addressOfField( obj1, fieldName );
        long        objSize2        = JFreeMemoryService.sizeOf( obj2 );
        long        objAddress2     = JFreeMemoryService.addressOf( obj2 );
        long        fieldAddress2   = JFreeMemoryService.addressOfField( obj2, fieldName );

        System.out.println( "Object Size 1    : " + objSize1 );
        System.out.println( "Object Address 1 : " + JFreeMemoryService.toHexAddress( objAddress1 ) );
        System.out.println( "Field Address 1  : " + JFreeMemoryService.toHexAddress( fieldAddress1 ) );
        
        System.out.println( "Object Size 2    : " + objSize2 );
        System.out.println( "Object Address 2 : " + JFreeMemoryService.toHexAddress( objAddress2 ) );
        System.out.println( "Field Address 2  : " + JFreeMemoryService.toHexAddress( fieldAddress2 ) );
        
        int newFieldValue1 = 200;
        System.out.println( "Get value of " + fieldName + " : " + unsafe.getInt( fieldAddress1 ) );
        System.out.println( "Set value of " + fieldName + " with " + newFieldValue1 + " by direct memory access" );
        unsafe.putInt( fieldAddress1, newFieldValue1 );
        System.out.println( "Get value of " + fieldName + " : " + unsafe.getInt( fieldAddress1 ) );
        
        int newFieldValue2 = 300;
        System.out.println( "Get value of " + fieldName + " : " + unsafe.getInt( fieldAddress2 ) );
        System.out.println( "Set value of " + fieldName + " with " + newFieldValue2 + " by direct memory access" );
        unsafe.putInt( fieldAddress2, newFieldValue2 );
        System.out.println( "Get value of " + fieldName + " : " + unsafe.getInt( fieldAddress2 ) );
        
        MyObject restoredObj1 = JFreeMemoryService.getObject( objAddress1 );
        System.out.println( restoredObj1.getI( ) );
        
        MyObject restoredObj2 = JFreeMemoryService.getObject( objAddress2 );
        System.out.println( restoredObj2.getI( ) );
        
        System.out.println( "Before object change ..." );
        System.out.println( "Get value " + fieldName + " of obj1 : " + obj1.getI( ) );
        System.out.println( "Get value " + fieldName + " of obj3 : " + obj3.getI( ) );
        JFreeMemoryService.changeObject( obj1, obj3 );
        System.out.println( "After object change ..." );
        System.out.println( "Get value " + fieldName + " of obj1 : " + obj1.getI( ) );
        System.out.println( "Get value " + fieldName + " of obj3 : " + obj3.getI( ) );
	 */

	@Test
	public void valueRetrievedAndSetSuccessfullyWithDirectMemoryAccess() throws SecurityException, NoSuchFieldException {
		SampleClass obj = new SampleClass();
		
		long addressOfField_b = directMemoryService.addressOfField(obj, "b");
		long addressOfField_i = directMemoryService.addressOfField(obj, "i");
		long addressOfField_l = directMemoryService.addressOfField(obj, "l");
		
		Assert.assertEquals(100, directMemoryService.getByte(addressOfField_b));
		Assert.assertEquals(5, directMemoryService.getInt(addressOfField_i));
		Assert.assertEquals(10, directMemoryService.getLong(addressOfField_l));
		
		directMemoryService.putByte(addressOfField_b, (byte)10); // Note that b is final static field
		directMemoryService.putInt(addressOfField_i, 55);
		directMemoryService.putLong(addressOfField_l, 100);

		Assert.assertEquals(10, directMemoryService.getByte(addressOfField_b));
		Assert.assertEquals(55, directMemoryService.getInt(addressOfField_i));
		Assert.assertEquals(100, directMemoryService.getLong(addressOfField_l));
	}
	
	@Test
	public void objectsSwitchedSuccessfullyWithDirectMemoryAccess() throws SecurityException, NoSuchFieldException {
		SampleClass objSource = new SampleClass();
		SampleClass objTarget = new SampleClass();
		
		objSource.setI(100);
		objSource.setL(1000);
		
		objTarget.setI(200);
		objTarget.setL(2000);

		Assert.assertEquals(200, objTarget.getI());
		Assert.assertEquals(2000, objTarget.getL());
		
		directMemoryService.changeObject(objSource, objTarget);
		
		Assert.assertEquals(100, objTarget.getI());
		Assert.assertEquals(1000, objTarget.getL());
	}	

}
