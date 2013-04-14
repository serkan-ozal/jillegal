/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

public class ObjectTree {

	public static void dump(PrintWriter pw, Object root) {
		Node nodeTree = Node.create(root);
	    printTree(new StringBuilder(), new StringBuilder(), pw, nodeTree);
	}

	public static String dump(Object root) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    dump(pw, root);
	    pw.flush();
	    return sw.toString();
	}
	  
	private static class Node {
		
	    private String name;
	    private List<Node> children;
	    
	    private long shallowSize;
	    private long deepSize;
	    
	    public Node(String name, Object delegate) {
	    	this.name = name;
	      
	    	if (delegate != null) {
	    		shallowSize = JvmUtil.shallowSizeOf(delegate);
	    		deepSize = shallowSize;
	    	}
	    }
	    
	    private void addChild(Node node) {
	    	if (children == null) {
	    		children = new ArrayList<Node>();
	    	}
	    	children.add(node);
	    	deepSize += node.deepSize;
	    }
	    
	    public static Node create(Object delegate) {
	    	return create("root", delegate, new IdentityHashMap<Object,Integer>());
	    }

	    public static Node create(String prefix, Object delegate, IdentityHashMap<Object,Integer> seen) {
	    	if (delegate == null) {
	    		throw new IllegalArgumentException();
	    	}
	      
	    	if (seen.containsKey(delegate)) {
	    		return new Node("[seen " + uniqueName(delegate, seen) + "]", null);
	    	}
	    	seen.put(delegate, seen.size());
	      
	    	Class<?> clazz = delegate.getClass();
	    	if (clazz.isArray()) {
	    		Node parent = new Node(prefix + " => " + clazz.getSimpleName(), delegate);
	    		if (clazz.getComponentType().isPrimitive()) {
	    			return parent;
	    		} 
	    		else {
	    			final int length = Array.getLength(delegate);
	    			for (int i = 0; i < length; i++) {
	    				Object value = Array.get(delegate, i);
	    				if (value != null) {
	    					parent.addChild(create("[" + i + "]", value, seen));
	    				}
	    			}
	    			return parent;
	    		}
	    	}
	    	else {
	    		List<Field> declaredFields = new ArrayList<Field>();
	    		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
	    			Field[] fields = c.getDeclaredFields();
	    			AccessibleObject.setAccessible(fields, true);
	    			declaredFields.addAll(Arrays.asList(fields));
	    		}
	    		Collections.sort(declaredFields, 
	    			new Comparator<Field>() {
				          @Override
				          public int compare(Field o1, Field o2) {
				        	  return o1.getName().compareTo(o2.getName());
				          }
	    	  		});
	        
	    		Node parent = new Node(prefix + " => " + uniqueName(delegate, seen), delegate);
	    		for (Field f : declaredFields) {
	    			try {
	    				if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
	    					Object fValue = f.get(delegate);
	    					if (fValue != null) {
	    						parent.addChild(create(f.getType().getSimpleName() + " " + f.getName(), fValue, seen));
	    					} 
	    					else {
	    						parent.addChild(new Node(f.getType().getSimpleName() + " " + f.getName() + " => null", null));
	    					}
	    				}
	    			} 
	    			catch (Exception e) {
	    				throw new RuntimeException(e);
	    			}
	    		}
	    		return parent;
	    	}
	    }
	    
	    private static String uniqueName(Object t, IdentityHashMap<Object,Integer> seen) {
	    	return "<" + t.getClass().getSimpleName() + "#" + seen.get(t) + ">";
	    }
	    
	    public String getName() {
	    	return name;
	    }
	    
	    public boolean hasChildren() {
	    	return children != null && !children.isEmpty();
	    }
	    
	    public List<Node> getChildren() {
	    	return children;
	    }
	    
	}
	  
	private static void printTree(StringBuilder prefix, StringBuilder line, PrintWriter pw, Node node) {
	    line.append(node.getName());
	    pw.println(String.format("%,8d %,8d  %s", node.deepSize, node.shallowSize, line.toString()));
	    line.setLength(0);
	    
	    if (node.hasChildren()) {
	    	int pLen = prefix.length();
	    	for (Iterator<Node> i = node.getChildren().iterator(); i.hasNext();) {
	    		Node next = i.next();
	    		line.append(prefix.toString());
	    		line.append("+- ");
	    		prefix.append(i.hasNext() ? "|  " : "   ");
	    		printTree(prefix, line, pw, next);
	    		prefix.setLength(pLen);
	    	}
	    }
	}
	  
}
