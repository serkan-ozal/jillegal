/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util.compressedoops.hotspot;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.compressedoops.CompressedOopsInfo;

public class HotspotMemoryLayoutBasedCompressedOopsInfoProvider implements HotspotCompressedOopsInfoProvider {

	@Override
	public CompressedOopsInfo getCompressedOopsInfo(Unsafe unsafe, int oopSize, 
			int addressSize, int objectAlignment, boolean isCompressedRef) {
		if (isHotspotJvm()) {
			return findCompressedOopsInfo(unsafe, addressSize, objectAlignment, isCompressedRef);
		}
		else {
			return null;
		}
	}
	
	private boolean isHotspotJvm() {
		String name =  System.getProperty("java.vm.name").toLowerCase();
		return name.contains("hotspot") || name.contains("openjdk");
	}
	
	private boolean isJava6() {
		return System.getProperty("java.specification.version").equals("1.6");
	}
	
	private boolean isJava7() {
		return System.getProperty("java.specification.version").equals("1.7");
	}
	
	private boolean isJava8() {
		return System.getProperty("java.specification.version").equals("1.8");
	}
	
	private CompressedOopsInfo findCompressedOopsInfo(Unsafe unsafe, int addressSize, 
			int objectAlignment, boolean isCompressedRef) {
        CompressedOopsInfo compressedOopsInfo = 
        		createCompressedOopsProvider(unsafe, addressSize, objectAlignment, isCompressedRef).
        			provideCompressedOopsInfo(unsafe, addressSize, objectAlignment, isCompressedRef);
        if (compressedOopsInfo != null) {
            return compressedOopsInfo;
        } 
        else {
            return 
            	new DefaultCompressedOopsInfoProvider().
            		provideCompressedOopsInfo(unsafe, addressSize, objectAlignment, isCompressedRef);
        }
    }

    private CompressedOopsInfoProvider createCompressedOopsProvider(Unsafe unsafe, int addressSize, 
			int objectAlignment, boolean isCompressedRef) {
        if (isHotspotJvm()) {
            if (isJava6()) {
                return new Java6HotspotJvmCompressedOopsInfoProvider(unsafe, addressSize, 
                		objectAlignment, isCompressedRef);
            } 
            else if (isJava7()) {
                return new Java7HotspotJvmCompressedOopsInfoProvider(unsafe, addressSize, 
                		objectAlignment, isCompressedRef);
            } 
            else if (isJava8()) {
                return new Java8HotspotJvmCompressedOopsInfoProvider(unsafe, addressSize, 
                		objectAlignment, isCompressedRef);
            } 
        } 
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////

    private static class CompressedOopsProviderCavy {

        @SuppressWarnings("unused")
        Object obj1;
        
        @SuppressWarnings("unused")
        Object obj2;

    }

    private interface CompressedOopsInfoProvider {

        CompressedOopsInfo provideCompressedOopsInfo(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef);

    }

    private class DefaultCompressedOopsInfoProvider implements CompressedOopsInfoProvider {

        @Override
        public CompressedOopsInfo provideCompressedOopsInfo(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef) {
            /*
             * When running with CompressedOops on 64-bit platform, the address
             * size reported by Unsafe is still 8, while the real reference
             * fields are 4 bytes long. Try to guess the reference field size
             * with this naive trick.
             */
            int oopSize = -1;
            try {
                long off1 = unsafe.objectFieldOffset(CompressedOopsProviderCavy.class.getField("obj1"));
                long off2 = unsafe.objectFieldOffset(CompressedOopsProviderCavy.class.getField("obj2"));
                oopSize = (int) Math.abs(off2 - off1);
            } 
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            if (oopSize != unsafe.addressSize()) {
                switch (oopSize) {
	                case 8:
	                    return new CompressedOopsInfo(0, log2p(objectAlignment));
	                case 16:
	                    return new CompressedOopsInfo(0, log2p(objectAlignment));
	                default:
	                    throw new IllegalStateException(
	                            "Unsupported address size for compressed reference shifting: " + oopSize);
                }
            } 
            else {
                return new CompressedOopsInfo(false);
            }
        }

    }

    private static int log2p(int x) {
        int r = 0;
        while ((x >>= 1) != 0) {
            r++;
        }    
        return r;
    }

    private abstract class AbstractHotspotJvmCompressedOopsInfoProvider
            implements CompressedOopsInfoProvider {

        protected final int COMPRESSED_OOP_SHIFT_SIZE;
        protected final HotspotJvmClassAddressFinder hotspotJvmClassAddressFinder;
        protected final boolean compressedOopsHandlingStrategyForBbjectAndClassIsDifferent;

        protected AbstractHotspotJvmCompressedOopsInfoProvider(
                boolean compressedOopsHandlingStrategyForBbjectAndClassIsDifferent,
                Unsafe unsafe, int addressSize, int objectAlignment, boolean isCompressedRef) {
            this.compressedOopsHandlingStrategyForBbjectAndClassIsDifferent = 
            		compressedOopsHandlingStrategyForBbjectAndClassIsDifferent;
            this.COMPRESSED_OOP_SHIFT_SIZE = log2p(objectAlignment);
            this.hotspotJvmClassAddressFinder = 
            		createClassAddressFinder(unsafe, addressSize, objectAlignment, isCompressedRef);
        }

        @Override
        public CompressedOopsInfo provideCompressedOopsInfo(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef) {
            long nativeAddressOfClass = 
            		hotspotJvmClassAddressFinder.nativeAddressOfClass(CompressedOopsProviderCavy.class);
            long jvmAddressOfClass = 
            		hotspotJvmClassAddressFinder.jvmAddressOfClass(CompressedOopsProviderCavy.class);
            if (!compressedOopsHandlingStrategyForBbjectAndClassIsDifferent) {
                if (nativeAddressOfClass == jvmAddressOfClass) {
                    return new CompressedOopsInfo(0L, 0);
                } 
                else {
                    long shiftedAddress = jvmAddressOfClass << COMPRESSED_OOP_SHIFT_SIZE;
                    return new CompressedOopsInfo(nativeAddressOfClass - shiftedAddress, COMPRESSED_OOP_SHIFT_SIZE);
                }
            } 
            else {
                long nativeAddressOfClassInstance = 
                		hotspotJvmClassAddressFinder.nativeAddressOfClassInstance(CompressedOopsProviderCavy.class);
                long jvmAddressOfClassInstance = 
                		hotspotJvmClassAddressFinder.jvmAddressOfClassInstance(CompressedOopsProviderCavy.class);

                long baseAddressForObjectPointers = 0L;
                int shiftSizeForObjectPointers = 0;
                long baseAddressForClassPointers = 0L;
                int shiftSizeForClassPointers = 0;

                if (nativeAddressOfClass != jvmAddressOfClass) {
                    long shiftedAddress = jvmAddressOfClass << COMPRESSED_OOP_SHIFT_SIZE;
                    baseAddressForObjectPointers = nativeAddressOfClass - shiftedAddress;
                    shiftSizeForObjectPointers = COMPRESSED_OOP_SHIFT_SIZE;
                }

                if (nativeAddressOfClassInstance != jvmAddressOfClassInstance) {
                    long shiftedAddress = jvmAddressOfClassInstance << COMPRESSED_OOP_SHIFT_SIZE;
                    baseAddressForClassPointers = nativeAddressOfClassInstance - shiftedAddress;
                    shiftSizeForClassPointers = COMPRESSED_OOP_SHIFT_SIZE;
                }

                return 
                	new CompressedOopsInfo(
                			baseAddressForObjectPointers,
                			shiftSizeForObjectPointers,
                			baseAddressForClassPointers, 
                			shiftSizeForClassPointers);
            }
        }

    }

    private class Java6HotspotJvmCompressedOopsInfoProvider 
    		extends AbstractHotspotJvmCompressedOopsInfoProvider {

        public Java6HotspotJvmCompressedOopsInfoProvider(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef) {
            super(false, unsafe, addressSize, objectAlignment, isCompressedRef);
        }

    }

    private class Java7HotspotJvmCompressedOopsInfoProvider 
    		extends AbstractHotspotJvmCompressedOopsInfoProvider {

        public Java7HotspotJvmCompressedOopsInfoProvider(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef) {
            super(false, unsafe, addressSize, objectAlignment, isCompressedRef);
        }

    }

    private class Java8HotspotJvmCompressedOopsInfoProvider 
    		extends AbstractHotspotJvmCompressedOopsInfoProvider {

        public Java8HotspotJvmCompressedOopsInfoProvider(Unsafe unsafe, int addressSize, 
        		int objectAlignment, boolean isCompressedRef) {
            super(true, unsafe, addressSize, objectAlignment, isCompressedRef);
        }

    }

    // ////////////////////////////////////////////////////////////////////////////

    private HotspotJvmClassAddressFinder createClassAddressFinder(Unsafe unsafe, int addressSize, 
    		int objectAlignment, boolean isCompressedRef) {
        if (isJava6()) {
            if (addressSize == 4) {
                return new Java6On32BitHotspotJvmClassAddressFinder(unsafe);
            } 
            else if (addressSize == 8) {
                if (isCompressedRef) {
                    return new Java6On64BitHotspotJvmWithCompressedOopsClassAddressFinder(unsafe);
                } 
                else {
                    return new Java6On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(unsafe);
                }
            } 
            else {
                throw new IllegalStateException("Unsupported address size: " + addressSize + " !");
            }
        } 
        else if (isJava7()) {
            if (addressSize == 4) {
                return new Java7On32BitHotspotJvmClassAddressFinder(unsafe);
            } 
            else if (addressSize == 8) {
                if (isCompressedRef) {
                    return new Java7On64BitHotspotJvmWithCompressedOopsClassAddressFinder(unsafe);
                } 
                else {
                    return new Java7On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(unsafe);
                }
            } 
            else {
                throw new IllegalStateException("Unsupported address size: " + addressSize + " !");
            }
        } 
        else if (isJava8()) {
            if (addressSize == 4) {
                return new Java8On32BitHotspotJvmClassAddressFinder(unsafe);
            } 
            else if (addressSize == 8) {
                if (isCompressedRef) {
                    return new Java8On64BitHotspotJvmWithCompressedOopsClassAddressFinder(unsafe);
                } 
                else {
                    return new Java8On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(unsafe);
                }
            } 
            else {
                throw new IllegalStateException("Unsupported address size: " + addressSize + " !");
            }
        } 
        else {
            throw new IllegalStateException("Unsupported Java version: " + 
            		System.getProperty("java.specification.version"));
        }
    }

    private interface HotspotJvmClassAddressFinder {

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_6 = 8L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6 = 12L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6 = 16L;

        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_6 = 60L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6 = 112L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6 = 112L;

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_6 = 32L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6 = 56L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6 = 56L;

        // ////////////////////////////////////////////////////////////////////////////

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_7 = 80L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7 = 84L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7 = 160L;

        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_7 = 64L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7 = 120L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7 = 120L;

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_7 = 36L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7 = 64L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7 = 64L;

        // ////////////////////////////////////////////////////////////////////////////

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_8 = 64L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8 = 64L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8 = 120L;

        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_8 = 56L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8 = 48L;
        long CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8 = 104L;

        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_8 = 28L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8 = 48L;
        long CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8 = 48L;

        // ////////////////////////////////////////////////////////////////////////////

        long jvmAddressOfClassInstance(Class<?> clazz);

        long nativeAddressOfClassInstance(Class<?> clazz);

        long jvmAddressOfClass(Class<?> clazz);

        long nativeAddressOfClass(Class<?> clazz);

    }

    private abstract class AbstractHotspotJvmClassAddressFinder
            implements HotspotJvmClassAddressFinder {

    	protected final Unsafe UNSAFE;
        protected final long OBJECT_ARRAY_BASE_OFFSET;

        protected final Object[] temp = new Object[1];

        protected long classDefPointerOffsetInClassInst;
        protected long classInstPointerOffsetInClassDef;
        protected long classDefPointerOffsetInClassDef;

        protected AbstractHotspotJvmClassAddressFinder(Unsafe unsafe) {
        	UNSAFE = unsafe;
        	OBJECT_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(Object[].class);
        }

        protected AbstractHotspotJvmClassAddressFinder(
        		Unsafe unsafe,
                long classDefPointerOffsetInClassInst,
                long classInstPointerOffsetInClassDef,
                long classDefPointerOffsetInClassDef) {
        	this(unsafe);
            this.classDefPointerOffsetInClassInst = classDefPointerOffsetInClassInst;
            this.classInstPointerOffsetInClassDef = classInstPointerOffsetInClassDef;
            this.classDefPointerOffsetInClassDef = classDefPointerOffsetInClassDef;
        }

        protected long normalize(int value) {
            return value & 0xFFFFFFFFL;
        }

        @Override
        public synchronized long jvmAddressOfClassInstance(Class<?> clazz) {
            try {
                temp[0] = clazz;
                return normalize(UNSAFE.getInt(temp, OBJECT_ARRAY_BASE_OFFSET));
            } finally {
                temp[0] = null;
            }
        }

        @Override
        public synchronized long nativeAddressOfClassInstance(Class<?> clazz) {
            try {
                UNSAFE.putInt(temp, OBJECT_ARRAY_BASE_OFFSET, (int) jvmAddressOfClass(clazz));
                Object o = temp[0];
                return UNSAFE.getInt(o, classInstPointerOffsetInClassDef);
            } 
            finally {
                temp[0] = null;
            }
        }

        @Override
        public synchronized long jvmAddressOfClass(Class<?> clazz) {
            return normalize(UNSAFE.getInt(clazz, classDefPointerOffsetInClassInst));
        }

        @Override
        public synchronized long nativeAddressOfClass(Class<?> clazz) {
            try {
                UNSAFE.putInt(temp, OBJECT_ARRAY_BASE_OFFSET, (int) jvmAddressOfClass(clazz));
                Object o = temp[0];
                return UNSAFE.getInt(o, classDefPointerOffsetInClassDef);
            } 
            finally {
                temp[0] = null;
            }
        }

    }

    // ////////////////////////////////////////////////////////////////////////////

    private class Java6On32BitHotspotJvmClassAddressFinder 
    		extends AbstractHotspotJvmClassAddressFinder {

        private Java6On32BitHotspotJvmClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_6,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_6,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_6);
        }

    }

    private class Java6On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java6On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_6);
        }

    }

    private class Java6On64BitHotspotJvmWithCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java6On64BitHotspotJvmWithCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super( 	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_6);
        }

    }

    // ////////////////////////////////////////////////////////////////////////////

    private class Java7On32BitHotspotJvmClassAddressFinder 
    		extends AbstractHotspotJvmClassAddressFinder {

        private Java7On32BitHotspotJvmClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_7,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_7,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_7);
        }

    }

    private class Java7On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java7On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_7);
        }

    }

    private class Java7On64BitHotspotJvmWithCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java7On64BitHotspotJvmWithCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_7);
        }

    }

    // ////////////////////////////////////////////////////////////////////////////

    private class Java8On32BitHotspotJvmClassAddressFinder 
    		extends AbstractHotspotJvmClassAddressFinder {

        private Java8On32BitHotspotJvmClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_32_BIT_FOR_JAVA_8,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_8,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_32_BIT_FOR_JAVA_8);
        }

    }

    private class Java8On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java8On64BitHotspotJvmWithoutCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super( 	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITHOUT_COMPRESSED_REF_FOR_JAVA_8);
        }

    }

    private class Java8On64BitHotspotJvmWithCompressedOopsClassAddressFinder
            extends AbstractHotspotJvmClassAddressFinder {

        private Java8On64BitHotspotJvmWithCompressedOopsClassAddressFinder(Unsafe unsafe) {
            super(	unsafe,
            		CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_INSTANCE_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8,
                    CLASS_INSTANCE_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8,
                    CLASS_DEFINITION_POINTER_OFFSET_IN_CLASS_DEFINITION_64_BIT_WITH_COMPRESSED_REF_FOR_JAVA_8);
        }

    }

    // ////////////////////////////////////////////////////////////////////////////
	
}
