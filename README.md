1. What is Jillegal?
==============

**Jillegal** is a library including unknown tricks of Java. It abstracts developer from low-level details to implement those tricks. Its design and logic are based on **Java 8** and **JRockit** so it can be used at only **_Java 8 and JRockit platforms_**. Java 6 and Java 7 supports are in progress and as soon as possible they will be released. Demo application is avaiable at [https://github.com/serkan-ozal/jillegal-demo](https://github.com/serkan-ozal/jillegal-demo). 

Currently it has three main modules: **OffHeap**, **Instrumentation** and **In Memory Compiler**. 

1.1. OffHeap Module
-------
Design and logic of Jillegal OffHeap module different from all of the other offheap frameworks. It doesn't serilalize/deserialize objects to/from allocated offheap memory region. Because objects already lives on offheap and GC doesn't track them :). With this feature, all objects in pool are exist as sequential at memory, so sequential accessing to them is faster. Because, they will be fetched to CPU cache together as limited size of CPU cache.

1.2. Instrumentation Module
-------

Instrumenting and redefining any Java class, interface, ... (even core Java classes) at runtime with developer friendly API (with Builder Pattern based design) is supported. You can add your custom pre/post listeners to method and constructor invocations dynamically. It serves a platform to develop your custom AOP framework. It uses Java Instrumentation API but adding extra VM argument (like `-javaagent:<jarpath>[=<options>]` is not required. **Jillegal** has its own internal agent and it can enable it's agent at runtime dynamically.

1.3. In Memory Compiler Module
-------

Compiling Java and Groovy source codes in memory is supported. You can compile your Java and Groovy based source codes at runtime in memory and can get its compiled class.

2. Compile and Build
=======

1. First clone code from GitHub.
   `git clone https://github.com/serkan-ozal/jillegal.git`

2. To set **JDK / JRE**  installation/home directory:
 
	2.1. Add an environment variable named `JAVA8_HOME` points to **JDK 8 / JRE 8**  installation/home directory.

	2.2. Add an environment variable named `JROCKIT_HOME` points to **JRockit JDK / JRockit JRE**  installation/home directory.
	
3. Compile with maven.
   `mvn clean install`

3. Installation
=======

In your `pom.xml`, you must add repository and dependency for **Jillegal**. 
You can change `jillegal.version` to any existing **Jillegal** library version.
Latest version is `2.1-SNAPSHOT`.

~~~~~ xml
...
<properties>
    ...
    <jillegal.version>2.1-SNAPSHOT</jillegal.version>
    ...
</properties>
...
<dependencies>
    ...
	<dependency>
		<groupId>tr.com.serkanozal</groupId>
		<artifactId>jillegal</artifactId>
		<version>${jillegal.version}</version>
	</dependency>
	...
</dependencies>
...
<repositories>
	...
	<repository>
		<id>serkanozal-maven-repository</id>
		<url>https://github.com/serkan-ozal/maven-repository/raw/master/</url>
	</repository>
	...
</repositories>
...
~~~~~

For **JRockit** usage, change **`artifactId`** to **`jillegal-jrockit`** in dependency such as:

~~~~~ xml
...
	<dependency>
		<groupId>tr.com.serkanozal</groupId>
		<artifactId>jillegal-jrockit</artifactId>
		<version>${jillegal.version}</version>
	</dependency>
...
~~~~~

4. Usage
=======

To initialize Jillegal and find Jillegal Aware classes, there are two ways:

1) You just need to call explicitly making aware method at startup in anywhere of your application.

~~~~~ java
...

tr.com.serkanozal.jillegal.Jillegal.init();

...
~~~~~

or

2) You can extend your main class from **`tr.com.serkanozal.jillegal.util.JillegalAware`** class.

~~~~~ java
...

public class JillegalDemo extends JillegalAware {

	public static void main(String[] args) {
	
		...
	
	}
	
}	

...
~~~~~

4.1. Jillegal Aware OffHeap Pool
-------

To make any of your class, just annotate it with **`tr.com.serkanozal.jillegal.config.annotation.JillegalAware`** annotation. So it will be detected by Jillegal on initialize cycle and will be instrumented automatically to be aware of Jillegal.

~~~~~ java
public class SampleClassWrapper {

	private SampleClass sampleClass;

	public SampleClass getSampleClass() {
		return sampleClass;
	}

	public void setSampleClass(SampleClass sampleClass) {
		this.sampleClass = sampleClass;
	}

}
~~~~~

~~~~~ java
@JillegalAware
public class JillegalAwareSampleClassWrapper {

	@OffHeapObject
	private SampleClass sampleClass;

	@OffHeapArray(length = 1000)
	private SampleClass[] sampleClassArray;

	public SampleClass getSampleClass() {
		return sampleClass;
	}

	public void setSampleClass(SampleClass sampleClass) {
		this.sampleClass = sampleClass;
	}

	public SampleClass[] getSampleClassArray() {
		return sampleClassArray;
	}

	public void setSampleClassArray(SampleClass[] sampleClassArray) {
		this.sampleClassArray = sampleClassArray;
	}

}
~~~~~

~~~~~ java
public class SampleClass {

	private int i1 = 5;
	private int i2 = 10;
	private int order;
	private SampleLinkClass link;

	public int getI1() {
		return i1;
	}

	public int getI2() {
		return i2;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public SampleLinkClass getLink() {
		return link;
	}

	public void setLink(SampleLinkClass link) {
		this.link = link;
	}
	
}
~~~~~

~~~~~ java
public class SampleLinkClass {

	private long linkNo;

	public long getLinkNo() {
		return linkNo;
	}

	public void setLinkNo(long linkNo) {
		this.linkNo = linkNo;
	}
	
}
~~~~~

~~~~~ java

JillegalAwareSampleClassWrapper sampleClassWrapper = new JillegalAwareSampleClassWrapper();

sampleClassWrapper.getSampleClass().setOrder(-1);

SampleClass[] objArray = sampleClassWrapper.getSampleClassArray();
    	
for (int i = 0; i < objArray.length; i++) {
    	SampleClass obj = objArray[i];
    	obj.setOrder(i);
    	System.out.println("Order value of auto injected off-heap object field has been set to " + i);
}

System.out.println("Order value of sample object at off heap pool: " + 
sampleClassWrapper.getSampleClass().getOrder());
    	
for (int i = 0; i < objArray.length; i++) {
    	SampleClass obj = objArray[i];
    	System.out.println("Order value of " + i + ". object at off heap pool: " + obj.getOrder());
}

~~~~~

4.2. Eager Referenced Object OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

ObjectOffHeapPoolCreateParameterBuilder<SampleClass> offHeapPoolParameterBuilder = 
				new ObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
						type(SampleClass.class).
						objectCount(ELEMENT_COUNT).
						makeOffHeapableAsAuto(true).
						referenceType(ObjectPoolReferenceType.EAGER_REFERENCED);
ObjectOffHeapPoolCreateParameter<SampleClass> offHeapPoolParameter = offHeapPoolParameterBuilder.build();
EagerReferencedObjectOffHeapPool<SampleClass> eagerReferencedObjectPool = 
				offHeapService.createOffHeapPool(offHeapPoolParameter);
for (int i = 0; i < ELEMENT_COUNT; i++) {
	SampleClass obj = eagerReferencedObjectPool.get();
	...
}

~~~~~

4.3. Lazy Referenced Object OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

ObjectOffHeapPoolCreateParameterBuilder<SampleClass> offHeapPoolParameterBuilder = 
				new ObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
						type(SampleClass.class).
						objectCount(ELEMENT_COUNT).
						makeOffHeapableAsAuto(true).
						referenceType(ObjectPoolReferenceType.LAZY_REFERENCED);
ObjectOffHeapPoolCreateParameter<SampleClass> offHeapPoolParameter = offHeapPoolParameterBuilder.build();
LazyReferencedObjectOffHeapPool<SampleClass> lazyReferencedObjectPool = 
				offHeapService.createOffHeapPool(offHeapPoolParameter);
for (int i = 0; i < ELEMENT_COUNT; i++) {
	SampleClass obj = lazyReferencedObjectPool.get();
	...
}

~~~~~

4.4. Primitive Type Array OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

PrimitiveTypeArrayOffHeapPool<Integer, int[]> primitiveTypeArrayPool = 
				offHeapService.createOffHeapPool(
						new ArrayOffHeapPoolCreateParameterBuilder<Integer>().
								type(Integer.class).
								length(ELEMENT_COUNT).
								usePrimitiveTypes(true).
							build());

int[] primitiveArray = primitiveTypeArrayPool.getArray();

for (int i = 0; i < primitiveArray.length; i++) {
	primitiveArray[i] = i;
}

~~~~~

4.5. Complex Type Array OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

ComplexTypeArrayOffHeapPool<SampleClass, SampleClass[]> complexTypeArrayPool = 
				offHeapService.createOffHeapPool(
						new ArrayOffHeapPoolCreateParameterBuilder<SampleClass>().
								type(SampleClass.class).
								length(ELEMENT_COUNT).
								initializeElements(true).
							build());
							
SampleClass[] complexArray = complexTypeArrayPool.getArray();

for (int i = 0; i < complexArray.length; i++) {
	SampleClass obj = complexArray[i];
	...
}	

~~~~~

4.6. Extendable Eager Referenced Object OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

EagerReferencedObjectOffHeapPool<SampleClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
								type(SampleClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.EAGER_REFERENCED).
							build());

ExtendableObjectOffHeapPool<SampleClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());

for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
	SampleClass obj = extendableObjectPool.get();
	...
}	

~~~~~

4.7. Extendable Lazy Referenced Object OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

LazyReferencedObjectOffHeapPool<SampleClass> sequentialObjectPool = 
				offHeapService.createOffHeapPool(
						new ObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
								type(SampleClass.class).
								objectCount(ELEMENT_COUNT).
								referenceType(ObjectPoolReferenceType.LAZY_REFERENCED).
							build());

ExtendableObjectOffHeapPool<SampleClass> extendableObjectPool =
				offHeapService.createOffHeapPool(
						new ExtendableObjectOffHeapPoolCreateParameterBuilder<SampleClass>().
								forkableObjectOffHeapPool(sequentialObjectPool).
							build());

for (int i = 0; i < TOTAL_ELEMENT_COUNT; i++) {
	SampleClass obj = extendableObjectPool.get();
	...
}	

~~~~~

4.8. String OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

StringOffHeapPool stringPool = 
				offHeapService.createOffHeapPool(
						new StringOffHeapPoolCreateParameterBuilder().
								estimatedStringCount(STRING_COUNT).
								estimatedStringLength(ESTIMATED_STRING_LENGTH).
							build());
   
for (int i = 0; i < STRING_COUNT; i++) {
	System.out.println(stringPool.get("String " + i));
}
    	
~~~~~

4.9. Extendable String OffHeap Pool
-------
~~~~~ java

OffHeapService offHeapService = OffHeapServiceFactory.getOffHeapService();

DeeplyForkableStringOffHeapPool stringPool = 
				offHeapService.createOffHeapPool(
						new StringOffHeapPoolCreateParameterBuilder().
								estimatedStringCount(STRING_COUNT).
								estimatedStringLength(ESTIMATED_STRING_LENGTH).
							build());
   
ExtendableStringOffHeapPool extendableStringPool =
				offHeapService.createOffHeapPool(
						new ExtendableStringOffHeapPoolCreateParameterBuilder().
								forkableStringOffHeapPool(stringPool).
							build());

for (int i = 0; i < TOTAL_STRING_COUNT; i++) {
	System.out.println(stringPool.get("String " + i));
}

~~~~~

4.10. Instrumentation Module
-------
~~~~~ java

public class SampleClass {

	public SampleClass() {
		System.out.println("SampleInstrumentClass.SampleClassToInstrument()"); 
	}

	public void methodToIntercept() {
		System.out.println("SampleInstrumentClass.methodToIntercept()"); 
	}
	
}

~~~~~

~~~~~ java

Jillegal.init();

System.out.println("Before Intrumentation: ");
System.out.println("=====================================================");

SampleClass obj1 = new SampleClass();
obj1.methodToIntercept();

System.out.println("=====================================================");

System.out.println("After Intrumentation: ");
System.out.println("=====================================================");

InstrumentService instrumentService = InstrumentServiceFactory.getInstrumentService();
Instrumenter<SampleClass> inst = instrumentService.getInstrumenter(SampleClass.class);
GeneratedClass<SampleClass> redefinedClass =
	inst.
		insertBeforeConstructors(
			new BeforeConstructorInterceptor<SampleClass>() {
				@Override
				public void beforeConstructor(SampleClass o, Constructor<SampleClass> c, Object[] args) {
					System.out.println("Intercepted by Jillegal before constructor ...");
				}}).

		insertAfterConstructors("System.out.println(\"Intercepted by Jillegal after constructor ...\");").

		insertBeforeMethod("methodToIntercept", 
			new BeforeMethodInterceptor<SampleClass>() {
				@Override
				public void beforeMethod(SampleClass o, Method m, Object[] args) {
					System.out.println("Intercepted by Jillegal before methodToIntercept method ...");
				}}).

		insertAfterMethod("methodToIntercept", 
			"System.out.println(\"Intercepted by Jillegal after methodToIntercept method ...\");").
                    			
	build();

instrumentService.redefineClass(redefinedClass); 

SampleClass obj2 = new SampleClass();
obj2.methodToIntercept();
        
System.out.println("=====================================================");

~~~~~

5. Roadmap
=======

* Java 6 / Java 7 support for OffHeap module.

* IBM JVM support for OffHeap module.
