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

5. Roadmap
=======

* Distributed lock (**`ILock`**) will be supported.

* Distributed object will be supported.
