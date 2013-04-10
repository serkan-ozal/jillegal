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
		
		SampleClass[] sampleObjectArray = new SampleClass[100];
		SampleClass sc = new SampleClass();
		//c1 0f 69 ff : 1111 1111 0110 1001 0000 1111 1100 0001 
		//4b 13 69 ff : 1111 1111 0110 1001 0001 0011 0100 1011 
		for (int i = 0; i < 100; i++) {
	    	System.out.print(String.format("%02x ", directMemoryService.getByte(SampleClass.class, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
		
		for (int i = 0; i < 100; i++) {
	    	System.out.print(String.format("%02x ", directMemoryService.getByte(sampleObjectArray, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
    	
    	for (int i = 0; i < 100; i++) {
	    	System.out.print(String.format("%02x ", directMemoryService.getByte(sc, (long)i)));
			if ((i + 1) % 16 == 0) {
				System.out.println();
			}
    	}	
    	System.out.println();
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
