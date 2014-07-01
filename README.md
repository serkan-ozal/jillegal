1. What is Jillegal?
==============

**Jillegal** is a library including unknown tricks of Java. It abstracts developer from low-level details to implement those tricks. Its design and logic are based on Java 8 so it can be used at only Java 8 platform. Java 6 and Java 7 supports are in progress and as soon as possible they will be released. Demo application is avaiable at [https://github.com/serkan-ozal/jillegal-demo](https://github.com/serkan-ozal/jillegal-demo). 

Currently it has two main module: **OffHeap**  and **Instrumentation**. 

1.1. OffHeap Module
-------
Design and logic of Jillegal OffHeap module different from all of the other offheap frameworks. It doesn't serilalize/deserialize objects to/from allocated offheap memory region. Becuase objects already lives on offheap and GC doesn't track them :). With this feature, all objects in pool are exist as sequential at memory, so sequential accessing to them is faster. Because, they will be fetched to CPU cache together as limited size of CPU cache.

1.2. Instrumentation Module
-------

Instrumenting and redefining any Java class, interface, ... (even core Java classes) at runtime with developer friendly API (with Builder Pattern based design) is supported. You can add your custom pre/post listeners to method and constructor invocations dynamically. It serves a platform to develop your custom AOP framework. It uses Java Instrumentation API but adding extra VM argument (like `-javaagent:<jarpath>[=<options>]` is not required. **Jillegal** has its own internal agent and it can enable it's agent at runtime dynamically.

2. Compile and Build
=======

1. First clone code from GitHub.
   `git clone https://github.com/serkan-ozal/jillegal.git`

2. Add an environment variable named `JAVA8_HOME` points to **JDK 8 / JRE 8**  installation/home directory.

3. Compile with maven.
   `mvn clean install`

3. Installation
=======

In your `pom.xml`, you must add repository and dependency for **Jillegal**. 
You can change `jillegal.version` to any existing **Jillegal** library version.
Latest version is `2.0-SNAPSHOT`.

~~~~~ xml
...
<properties>
    ...
    <jillegal.version>2.0-SNAPSHOT</jillegal.version>
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

4. Usage
=======

4.2. Eagaer Referenced Object OffHeap Pool
-------
~~~~~ java

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

4.6. Instrumentation Module
-------

4.7. Instrumentation Module
-------

4.8. Instrumentation Module
-------

4.9. Instrumentation Module
-------

4.10. Instrumentation Module
-------

4.11. Instrumentation Module
-------

4.12. Instrumentation Module
-------

5. Roadmap
=======

* On the fly in memory compiler support under **Compiler module** for Java and Groovy lamguages under.

* Java 6 / Java 7 support for OffHeap module.
