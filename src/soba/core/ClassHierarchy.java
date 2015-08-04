package soba.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import soba.core.method.CallSite;
import soba.core.method.FieldAccess;

/**
 * This class represents a class hierarchy.
 */
public class ClassHierarchy implements IDynamicBindingResolver {

	private boolean frozen;
	
	private static final String JAVA_LANG_OBJECT = "java" + ClassInfo.PACKAGE_SEPARATOR + "lang" + ClassInfo.PACKAGE_SEPARATOR + "Object"; 
	
	private Map<String, ClassInfo> entries; // type -> a method list of the type
	private Map<String, String> parentClass;   // type -> its super type 
	private Map<String, List<String>> parentInterfaces;  // type -> its interfaces
	private Map<String, Set<String>> subtypes; // type -> a set of sub types 
	private Set<String> requestedClasses; // a set of type names that are queried but not found

	private static List<String> EMPTY = Collections.unmodifiableList(new ArrayList<String>(0)); 
	
	/**
	 * Creates a new <code>ClassHierarchy</code> instance.
	 */
	public ClassHierarchy() {
		frozen = false;
		
		subtypes = new HashMap<String, Set<String>>();
		parentClass = new HashMap<String, String>();
		parentInterfaces = new HashMap<String, List<String>>();
		entries = new HashMap<String, ClassInfo>();

		requestedClasses = new HashSet<String>();

	}
	
	/**
	 * Resolves dynamic binding of a method invocation. 
	 * @param cs is a method invocation
	 * @return an array of <code>MethodInfo</code> objects representing methods 
	 * that might be executed by the invocation.
	 * The return value is an empty array if no method declaration 
	 * matched to the invocation.
	 */
	@Override
	public MethodInfo[] resolveCall(CallSite cs) {
		return resolveCall(cs.getClassName(), cs.getMethodName(), cs.getDescriptor(), !cs.isStaticOrSpecial());
	}
	
	/**
	 * Resolves dynamic binding of a method invocation.
	 * @param className is a class name
	 * @param methodName is a method name
	 * @param methodDesc is a method descriptor
	 * @param dynamic is true if the invoked method is bound dynamically
	 * @return an array of <code>MethodInfo</code> objects representing methods 
	 * that might be executed by the invocation.
	 * The return value is an empty array if no method declaration 
	 * matched to the invocation.
	 */
	public MethodInfo[] resolveCall(String className, String methodName, String methodDesc, boolean dynamic) {
		if (!dynamic) {
			MethodInfo	m = resolveSpecialCall(className, methodName, methodDesc);
			return (m == null) ? new MethodInfo[0] : new MethodInfo[] {m};
		} else {
			return resolveDynamicCall(className, methodName, methodDesc);
		}
	}
	
	/**
	 * Resolve dynamic binding of a virtual method call.
	 * Please note that this method does not care about the detail of the method definition.
	 * If a static method or an undefined private method is specified, this method may return incorrect result.
	 * @param className specifies a class representing a receiver type.
	 * @param methodName specifies a method name.
	 * @param methodDesc specifies a method descriptor (without generics information). 
	 * @return MethodInfo object representing the specified method.
	 * The return value may be an emtpy if the method is not found.
	 */
	private MethodInfo[] resolveDynamicCall(String className, String methodName, String methodDesc) {
		assert className != null && methodName != null && methodDesc != null : "Parameters cannot be null."; 
		String targetTypeName = className;
		
		// Find the declaration of the called method.
		MethodInfo topDecl = findDeclaration(className, methodName, methodDesc);
		if (topDecl == null) return new MethodInfo[0];  // Not found
		
		List<MethodInfo> result = new ArrayList<>(16);
		if (topDecl.hasMethodBody()) result.add(topDecl);
	
		
		// We explicitly avoid array types, because arrays are not included in ClassHierarchy.
		if (!isArrayType(targetTypeName)) {
			// Find all implementation of the same method signature in subclasses.
			Set<String> checkedClasses = new HashSet<String>();
			Stack<String> classes = new Stack<String>();
			classes.add(targetTypeName);
			while (!classes.empty()) { 
				String currentClass = classes.pop();
	
				// skip the visited classes
				if (checkedClasses.contains(currentClass)) {
					continue;
				}
				checkedClasses.add(currentClass);
				
				ClassInfo currentClassInfo = getClassInfo(currentClass);
				if (currentClassInfo != null) {
					MethodInfo m = currentClassInfo.findMethod(methodName, methodDesc);
					if (m != null) {
						if (m.hasMethodBody() && (m != topDecl)) { 
							// m overrides the target class.
							result.add(m); 
						}
					}
					
					if ((m == null) || m.isOverridable()) { 
						// the method may be overridden by subclasses.
						for (String c: getSubtypes(currentClass)) {
							if (m != null && m.isPackagePrivate()) {
								// A package-private method can be overridden by only classes in the same package.
								if (isSamePackage(c, currentClass)) {
									classes.push(c);
								}
							} else {
								// Other methods can be overridden by sub-types.
								classes.push(c);
							}
						}
					}
				} else {
					// Skip a class that is not included in the class hierarchy.
				}
			}
			
		}
	
		MethodInfo[] resultArray = new MethodInfo[result.size()];
		for (int i=0; i<result.size(); ++i) {
			resultArray[i] = result.get(i);
		}
		return resultArray;
	}
	
	/**
	 * Resolve binding of a static/constructor call.
	 * @param className specifies a class representing a receiver type.
	 * @param methodName specifies a method name.
	 * @param methodDesc specifies a method descriptor (without generics information). 
	 * @return MethodInfo object representing the specified method.
	 * The return value may be null if the method is not found.
	 */
	public MethodInfo resolveSpecialCall(String className, String methodName, String methodDesc) {
		MethodInfo m = findDeclaration(className, methodName, methodDesc);
		if (m != null) {
			return m;
		} else {
			return null;
		}
	}
	
	/**
	 * Finds method declaration specified by typeName and signature.
	 * @param typeName is fully qualified domain name of a class/interface.
	 * @param signatureId identifies a method by its name and parameters.  
	 * To obtain a method signature, use MethodUtil.getMethodSignature.
	 * @return MethodDecl object of the nearest ancestor (including the class specified by typeName) 
	 * If no ancestor classes declare the method,
	 * MethodDecl comes from interfaces who declares the method.
	 * @throws ClassNotFoundException is thrown if typeName or its ancestors are not found in class hierarchy.
	 * @throws NoSuchMethodException is thrown if method is not found in ancestor classes and interfaces.
	 */
	private MethodInfo findDeclaration(String className, String methodName, String methodDesc) {
		// Find the nearest ancestor class implements the method
		String currentClass = className;
		while (currentClass != null) {
			ClassInfo currentClassInfo = getClassInfo(currentClass);
			if (currentClassInfo != null) {
				MethodInfo m = currentClassInfo.findMethod(methodName, methodDesc);
				if (m != null) {
					return m;
				} else {
					currentClass = getSuperClass(currentClass);
				}
			} else {
				if (isArrayType(currentClass)) { 
					currentClass = getSuperClass(currentClass);
					continue;
				}
				return null;
			}
		}
		
		// Search all interfaces
		LinkedList<String> worklist = new LinkedList<String>();
		currentClass = className;
		while (currentClass != null) {
			worklist.addAll(getSuperInterfaces(currentClass));
			currentClass = getSuperClass(currentClass);
		}
		// Find a method declaration in the interfaces
		while (!worklist.isEmpty()) {
			String interfaceName = worklist.pollFirst();
			ClassInfo currentClassInfo = getClassInfo(interfaceName);
			if (currentClassInfo != null) {
				MethodInfo m = currentClassInfo.findMethod(methodName, methodDesc);
				if (m != null) {
					return m;
				} else {
					// An interface may extend another interface.
					worklist.addAll(getSuperInterfaces(interfaceName));
				}
			} else {
				if (isArrayType(interfaceName)) { 
					// ignore array types
					continue;
				}
				// Skip an interfaceName that is not included in the class hierarchy.
				return null;
			}
		}
		
		// Method not found at all
		return null;
	}
	
	/**
	 * Finds a accessed <code>FieldInfo</code> object.
	 * @param access specifies a field access instruction.
	 * @return a <code>FieldInfo</code> object.
	 */
	public FieldInfo resolveField(FieldAccess access) {
		String owner = resolveFieldOwner(access);
		return getClassInfo(owner).findField(access.getFieldName(), access.getDescriptor());
	}

	/**
	 * Finds a class which has a field to be accessed. 
	 * @param access specifies a field access instruction.
	 * @return the name of a class which has a specified field.
	 */
	private String resolveFieldOwner(FieldAccess access) {
		if (access.isStatic()) {
			return resolveStaticFieldOwner(access.getClassName(), access.getFieldName(), access.getDescriptor());
		} else {
			return resolveInstanceFieldOwner(access.getClassName(), access.getFieldName(), access.getDescriptor());
		}
	}

	/**
	 * Finds a class which has an instance field to be accessed. 
	 * @param className specifies a class name in a field access instruction.
	 * The class may inherit a field from its parent.
	 * @param fieldName 
	 * @param fieldDesc
	 * @return a class name that defines the field specified by the arguments.
	 * The method may return null if an owner is not found.
	 */
	public String resolveInstanceFieldOwner(String className, String fieldName, String fieldDesc) {
    	String current = className;
		while (current != null) {
			ClassInfo c = getClassInfo(current);
			if (c != null) {
				if (c.findField(fieldName, fieldDesc) != null) {
					return current;
				} else {
					current = c.getSuperClass();
				}
			} else {
				// If a class is not registered, stop to resolve the owner.
				current = null;
			}
		}
		return null;
	}
	
	/**
	 * Finds a class which has a static field to be accessed. 
	 * @param className 
	 * @param fieldName
	 * @param fieldDesc
	 * @return a class name.
	 * @see JVM Specification Section 5.4.3.2.
	 * If two public fields are accessible from the specified className and fieldName,
	 * javac reports the ambiguous field reference as an error.
	 */
	public String resolveStaticFieldOwner(String className, String fieldName, String fieldDesc) {
		// Search the current class
		ClassInfo c = getClassInfo(className);
		if (c == null) return null;
		
		if (c != null) {
			if (c.findField(fieldName, fieldDesc) != null) {
				return className;
			}
		}

		// If not defined, search interface
		Stack<String> worklist = new Stack<String>();
		worklist.push(className);
		while (!worklist.isEmpty()) {
			String current = worklist.pop();
			c = getClassInfo(current);
			if (c != null) {
				if (c.findField(fieldName, fieldDesc) != null) {
					return current;
				} else {
					if (c.getInterfaces() != null) {
						worklist.addAll(c.getInterfaces());
					}
				}
			} else {
				// If c is not a registered class, ignore it. 
			}
		}
		
		// Recursively search a super class
    	c = getClassInfo(className);
    	if (c.getSuperClass() != null) return resolveStaticFieldOwner(c.getSuperClass(), fieldName, fieldDesc);
    	else return null;
	}
	
	/**
	 * Prevents further modifications to the object.
	 */
	public void freeze() {
		assert !frozen: "ClassHierarchy is already frozen."; 
		frozen = true;
	}
	
	/**
	 * @return true if this object is frozen. 
	 */
	public boolean isFrozen() {
		return frozen;
	}
	
	/**
	 * @return a set of class names which are requested 
	 * by client methods, but not involved in this class hierarchy.
	 */
	public Set<String> getRequestedClasses() {
		return requestedClasses;
	}
	
	/**
	 * @param className 
	 * @return a <code>ClassInfo</code> object specified by the class name.
	 */
	public ClassInfo getClassInfo(String className) {
		ClassInfo c = entries.get(className);
		if (c == null) {
			requestedClasses.add(className);			
		}
		return c;
	}
	
	/**
	 * @return the number of registered classes. 
	 */
	public int getClassCount() {
		return entries.size();
	}

	/**
	 * @return the registered class names. 
	 */
	public Iterable<String> getClasses() {
		return entries.keySet();
	}
	
	/**
	 * Compare package names for the specified two types.
	 * @param typeName1 specifies a type to be compared.
	 * The type name must be registered to the class hierarchy. 
	 * @param typeName2 also specifies a type to be comapred.
	 * @return true if the specified types belong to the same package.
	 */
	public boolean isSamePackage(String typeName1, String typeName2) {
		ClassInfo c1 = entries.get(typeName1);
		ClassInfo c2 = entries.get(typeName2);
		
		if (c1 == null) requestedClasses.add(typeName1);
		if (c2 == null) requestedClasses.add(typeName2);
		
		return (c1 != null)&&(c2 != null)&&(c1.getPackageName().equals(c2.getPackageName()));
	}
	
	/**
	 * @param className specifies a class.
	 * @return a super class name for the specified class.
	 * This method returns null for "java/lang/Object". 
	 * "java.lang.Object" is returned for an interface and an array type.
	 */
	public String getSuperClass(String className) { 
		if (isArrayType(className)) return JAVA_LANG_OBJECT;
		else {
			if (!parentClass.containsKey(className)) {
				requestedClasses.add(className);
			}
			return parentClass.get(className);
		}
	}
	
	/**
	 * @param className specifies a fully qualified class name. 
	 * @return interfaces implemented by the specified class.
	 * If the class has no interfaces, this method returns an empty collection.
	 * If the class is an interface and it extends another interface B,
	 * B is regarded as a super-interface of the class.
	 * If the specified class has no interfaces, 
	 * the result is an empty colleciton.
	 */
	public Collection<String> getSuperInterfaces(String className) {
		if (isArrayType(className)) return EMPTY;
		else if (parentInterfaces.containsKey(className)) { 
			return parentInterfaces.get(className);
		} else {
			if (!entries.containsKey(className)) requestedClasses.add(className);
			return EMPTY;
		}
	}
	
	/**
	 * List all direct and transitive super-types of a specified type. 
	 * @param className specifies a fully qualified class name. 
	 */
	public Collection<String> listAllSuperTypes(String className) {
		if (!entries.containsKey(className)) requestedClasses.add(className);

		Set<String> classes = new HashSet<String>();
		Queue<String> worklist = new LinkedList<String>();
		worklist.add(className);
		while (!worklist.isEmpty()) {
			String name = worklist.poll();
			String superClass = getSuperClass(name);
			if (superClass != null && !classes.contains(superClass)) {
				classes.add(superClass);
				worklist.add(superClass);
			}
			for (String s: getSuperInterfaces(name)) {
				if (s != null && !classes.contains(s)) {
					classes.add(s);
					worklist.add(s);
				}
			}
		}
		return classes;
	}
	
	/**
	 * @param typeName specifies a type name.
	 * @return true if the type name represents array types.
	 */
	public boolean isArrayType(String typeName) { 
		return typeName.endsWith("[]");
	}

	/**
	 * @return a collection of classes which extend/implement 
	 * the specified type.
	 * The result may be an empty collection.
	 */
	public Collection<String> getSubtypes(String typeName) {
		if (!entries.containsKey(typeName)) requestedClasses.add(typeName);

		if (subtypes.containsKey(typeName)) { 
			return subtypes.get(typeName);
		} else {
			return EMPTY;
		}
	}
	
	/**
	 * @param typeNames
	 * @return a collection of all the sub-types for the specified types.
	 */
	public Collection<String> getAllSubtypes(Iterable<String> typeNames) {
		Stack<String> worklist = new Stack<String>();
		for (String t: typeNames) {
			worklist.push(t);
		}
		
		HashSet<String> visited = new HashSet<String>();
		while (!worklist.isEmpty()) {
			String t = worklist.pop();
			if (visited.contains(t)) continue;
			
			visited.add(t);
			worklist.addAll(getSubtypes(t));
		}
		return visited;
	}

	/**
	 * This method registers a class info object to the hierarchy.
	 * This method calls registerSuperClass, registerSubtype and registerInterfaces. 
	 * @param c is a registered <code>ClassInfo</code> object.
	 */
	public void registerClass(ClassInfo c) {
		if (frozen) {
			throw new FrozenHierarchyException();
		}
		entries.put(c.getClassName(), c);
		registerSuperClass(c.getClassName(), c.getSuperClass());
		registerSubtype(c.getClassName(), c.getSuperClass());
		registerInterfaces(c.getClassName(), c.getInterfaces());
		for (String interfaceName: c.getInterfaces()) {
			registerSubtype(c.getClassName(), interfaceName);
		}
	}
	
	/**
	 * This method allows developers to manually modify the class hierarchy.
	 * @param current specifies a class name. 
	 * @param parent specifies the super class name of the current class.
	 */
	public void registerSuperClass(String current, String parent) {
		if (frozen) {
			throw new FrozenHierarchyException();
		}
		parentClass.put(current, parent);
		
	}

	/**
	 * This method allows developers to manually modify the type hierarchy.
	 * A subtype of a class is a subclass.
	 * A subtype of an interface is an implementation class. 
	 * @param typeName
	 * @param parentTypeName
	 */
	public void registerSubtype(String typeName, String parentTypeName) {
		if (frozen) {
			throw new FrozenHierarchyException();
		}
		if (subtypes.containsKey(parentTypeName)) {
			subtypes.get(parentTypeName).add(typeName);
		} else {
			HashSet<String> types = new HashSet<String>();
			types.add(typeName);
			subtypes.put(parentTypeName, types);
		}
	}
	
	/**
	 * This method allows developers to manually modify the type hierarchy.
	 * @param current
	 * @param interfaces
	 */
	public void registerInterfaces(String current, List<String> interfaces) {
		if (frozen) {
			throw new FrozenHierarchyException();
		}
		parentInterfaces.put(current, interfaces);
	}
	
	public class FrozenHierarchyException extends RuntimeException {
		private static final long serialVersionUID = -8288161390304221032L;
	}

}
