package soba.core.signature;

public class TypeConstants {

	public static final String BOOLEAN = "boolean";
	public static final String BYTE = "byte";
	public static final String CHAR = "char";
	public static final String SHORT = "short";
	public static final String INT = "int";
	public static final String LONG = "long";
	public static final String FLOAT = "float";
	public static final String DOUBLE = "double";
	public static final String VOID = "void";
	public static final String JAVA_STRING = "java/lang/String";
	public static final String UNKNOWN_TYPE = "UNKNOWN-TYPE";
	
	public static final String[] PRIMITIVE_TYPES = {
		BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE 
	};

	/**
	 * @param name specifies a type name.
	 * @return true if a given name is a primitive types
	 * (excluding "void").
	 */
	public static boolean isPrimitiveTypeName(String name) { 
		for (int i=0; i<PRIMITIVE_TYPES.length; ++i) {
			if (PRIMITIVE_TYPES[i].equals(name)) return true;
		}
		return false;
	}
	
	/**
	 * return name == primitive || name == void
	 * NOTE: name is normal type name. (NOT bytecode descriptor, such as I or Z)
	 * @param name
	 * @return
	 */
	public static boolean isPrimitiveOrVoid(String name) {
		return isPrimitiveTypeName(name) || VOID.equals(name);
	}
	
	/**
	 * return name == void
	 */
	public static boolean isVoid(String name) {
		return VOID.equals(name);
	}
	
	/**
	 * @param name specifies a type.
	 * @return the number of words to store the type.
	 * 2 is returned for double and long.  
	 */
	public static int getWordCount(String name) {
		if (DOUBLE.equals(name) || LONG.equals(name)) {
			return 2;
		} else {
			return 1;
		}
	}
	
	public static boolean isJavaString(String name) {
		return name != null && name.equals(JAVA_STRING);
	}
}
