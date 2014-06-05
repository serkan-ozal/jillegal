/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jillegal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Serkan ÖZAL
 */
public class ReflectionUtil {

	private static final Logger logger = Logger.getLogger(ReflectionUtil.class);
	
	private ReflectionUtil() {
		
	}
	
	public static Field getField(Class<?> cls, String fieldName) {
		while (cls != null && cls.equals(Object.class) == false) {
			Field field = null;
			try {
				field = cls.getDeclaredField(fieldName);
			} 
			catch (SecurityException e) {
				logger.error("Unable to get field " + fieldName + " from class " + cls.getName(), e);
			} 
			catch (NoSuchFieldException e) {
				logger.warn("Unable to get field " + fieldName + " from class " + cls.getName() + ". " + 
							 "If there is any super class for class " + cls.getName() + ", it will be tried for getting field ...");
			}
			if (field != null) {
				field.setAccessible(true);
				return field;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}
	
	public static List<Field> getAllFields(Class<?> cls) {
		List<Field> fields = new ArrayList<Field>();
		createFields(cls, fields, null);
		return fields;
	}

	public static List<Field> getAllFields(Class<?> cls, Class<? extends Annotation> annotationFilter) {
		List<Field> fields = new ArrayList<Field>();
		createFields(cls, fields, annotationFilter);
		return fields;
	}
	
	private static void createFields(Class<?> cls, List<Field> fields, 
			Class<? extends Annotation> annotationFilter) {
		if (cls == null || cls.equals(Object.class)) {
			return;
		}
		
		Class<?> superCls = cls.getSuperclass();
		createFields(superCls, fields, annotationFilter);
		
		for (Field f : cls.getDeclaredFields()) {
			f.setAccessible(true);
			if (annotationFilter == null) {
				fields.add(f);
			}	
			else {
				if (f.getAnnotation(annotationFilter) != null) {
					fields.add(f);
				}	
			}
		}	
	}
	
	public static Method getMethod(Class<?> cls, String methodName) {
		while (cls != null && cls.equals(Object.class) == false) {
			Method method = null;
			for (Method m : cls.getDeclaredMethods()) {
				if (m.getName().equals(methodName)) {
					method = m;
					break;
				}
			}
			if (method == null) {
				logger.warn("Unable to get method " + methodName + " from class " + cls.getName() + ". " + 
							 "If there is any super class for class " + cls.getName() + ", it will be tried for getting method ...");
			}
			else {
				method.setAccessible(true);
				return method;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}
	
	public static List<Method> getAllMethods(Class<?> cls) {
		List<Method> methods = new ArrayList<Method>();
		createMethods(cls, methods, null);
		return methods;
	}
	
	public static List<Method> getAllMethods(Class<?> cls, Class<? extends Annotation> annotationFilter) {
		List<Method> methods = new ArrayList<Method>();
		createMethods(cls, methods, annotationFilter);
		return methods;
	}
	
	private static void createMethods(Class<?> cls, List<Method> methods, 
			Class<? extends Annotation> annotationFilter) {
		if (cls == null || cls.equals(Object.class)) {
			return;
		}
		
		Class<?> superCls = cls.getSuperclass();
		createMethods(superCls, methods, annotationFilter);
		
		for (Method m : cls.getDeclaredMethods()) {
			m.setAccessible(true);
			if (annotationFilter == null) {
				methods.add(m);
			}	
			else {
				if (m.getAnnotation(annotationFilter) != null) {
					methods.add(m);
				}	
			}
		}	
	}
	
	public static boolean isPrimitiveType(String clsName) {
		if (clsName.equalsIgnoreCase("boolean")) {
			return true;
		}	
		else if (clsName.equalsIgnoreCase("byte")) {
			return true;
		}
		else if (clsName.equalsIgnoreCase("char")) {
			return true;
		}
		else if (clsName.equalsIgnoreCase("short")) {
			return true;
		}	
		else if (clsName.equalsIgnoreCase("int")) {
			return true;
		}	
		else if (clsName.equalsIgnoreCase("float")) {
			return true;
		}	
		else if (clsName.equalsIgnoreCase("long")) {
			return true;
		}	
		else if (clsName.equalsIgnoreCase("double")) {
			return true;
		}	
		else {
			return false;
		}
	}
	
	public static boolean isPrimitiveType(Class<?> cls) {
		if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
			return true;
		}	
		else if (cls.equals(byte.class) || cls.equals(Byte.class)) {
			return true;
		}	
		else if (cls.equals(char.class) || cls.equals(Character.class)) {
			return true;
		}	
		else if (cls.equals(short.class) || cls.equals(Short.class)) {
			return true;
		}	
		else if (cls.equals(int.class) || cls.equals(Integer.class)) {
			return true;
		}	
		else if (cls.equals(float.class) || cls.equals(Float.class)) {
			return true;
		}	
		else if (cls.equals(long.class) || cls.equals(Long.class)) {
			return true;
		}	
		else if (cls.equals(double.class) || cls.equals(Double.class)) {
			return true;
		}
		else if (cls.equals(String.class)) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	public static boolean isNonPrimitiveType(String clsName) {
		return !isPrimitiveType(clsName);
	}
	
	public static boolean isNonPrimitiveType(Class<?> cls) {
		return !isPrimitiveType(cls);
	}

	public static boolean isComplexType(Class<?> cls) {
		if (isPrimitiveType(cls)) {
			return false;
		}
		else if (cls.isEnum()) {
			return false;
		}
		else if (cls.equals(String.class)) {
			return false;
		}
		else if (isCollectionType(cls)) {
			return false;
		}
		else if (List.class.isAssignableFrom(cls)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static boolean isCollectionType(Class<?> cls) {
		if (cls.isArray()) {
			return true;
		}
		else if (List.class.isAssignableFrom(cls)) {
			return true;
		}
		else if (Set.class.isAssignableFrom(cls)) {
			return true;
		}
		else if (Map.class.isAssignableFrom(cls)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static Class<?> getNonPrimitiveType(String clsName) {
		if (clsName.equalsIgnoreCase("boolean")) {
			return Boolean.class;
		}	
		else if (clsName.equalsIgnoreCase("byte")) {
			return Byte.class;
		}
		else if (clsName.equalsIgnoreCase("char")) {
			return Character.class;
		}
		else if (clsName.equalsIgnoreCase("short")) {
			return Short.class;
		}	
		else if (clsName.equalsIgnoreCase("int")) {
			return Integer.class;
		}	
		else if (clsName.equalsIgnoreCase("float")) {
			return Float.class;
		}	
		else if (clsName.equalsIgnoreCase("long")) {
			return Long.class;
		}	
		else if (clsName.equalsIgnoreCase("double")) {
			return Double.class;
		}	
		else {
			try {
				return Class.forName(clsName);
			} 
			catch (ClassNotFoundException e) {
				logger.error("Unable to get class " + clsName, e);
				return null;
			}
		}	
	}
	
	public static Class<?> getNonPrimitiveType(Class<?> cls) {
		if (cls.equals(boolean.class)) {
			return Boolean.class;
		}	
		else if (cls.equals(byte.class)) {
			return Byte.class;
		}
		else if (cls.equals(char.class)) {
			return Character.class;
		}
		else if (cls.equals(short.class)) {
			return Short.class;
		}	
		else if (cls.equals(int.class)) {
			return Integer.class;
		}	
		else if (cls.equals(float.class)) {
			return Float.class;
		}	
		else if (cls.equals(long.class)) {
			return Long.class;
		}	
		else if (cls.equals(double.class)) {
			return Double.class;
		}	
		else {
			return cls;
		}	
	}
	
	public static boolean isDecimalType(Class<?> cls) {
		
		if (cls.equals(byte.class) || cls.equals(Byte.class)) {
			return true;
		}	
		else if (cls.equals(short.class) || cls.equals(Short.class)) {
			return true;
		}	
		else if (cls.equals(int.class) || cls.equals(Integer.class)) {
			return true;
		}	
		else if (cls.equals(long.class) || cls.equals(Long.class)) {
			return true;
		}	
		else {
			return false;
		}
	}	
	
}