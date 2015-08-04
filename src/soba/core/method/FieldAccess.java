package soba.core.method;

public class FieldAccess {

	private String className;
	private String fieldName;
	private String fieldDesc;
	private boolean isStatic;
	private boolean isGet; // true==GET, false==PUT 

	private FieldAccess(String className, String fieldName, String fieldDesc, boolean isStatic, boolean isGet) {
		this.className = className;
		this.fieldName = fieldName;
		this.fieldDesc = fieldDesc;
		this.isStatic = isStatic;
		this.isGet = isGet;
	}
	
	/**
	 * Creates a new <code>FieldAccess</code> instance representing a load instruction of a field.
	 * @param className is a class name of the accessed field.
	 * @param fieldName is a field name.
	 * @param fieldDesc is a descriptor of the field.
	 * @param isStatic is true if it is a static field.
	 * @return a <code>FieldAccess</code> object.
	 */
	public static FieldAccess createGetField(String className, String fieldName, String fieldDesc, boolean isStatic) {
		return new FieldAccess(className, fieldName, fieldDesc, isStatic, true);
	}

	/**
	 * Creates a new <code>FieldAccess</code> instance representing a store instruction of a field.
	 * @param className is a class name of the accessed field.
	 * @param fieldName is a field name.
	 * @param fieldDesc is a descriptor of the field.
	 * @param isStatic is true if it is a static field.
	 * @return a <code>FieldAccess</code> object.
	 */
	public static FieldAccess createPutField(String className, String fieldName, String fieldDesc, boolean isStatic) {
		return new FieldAccess(className, fieldName, fieldDesc, isStatic, false);
	}
	
	/**
	 * @return the class name of the field.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the field name.
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * @return the descriptor of the field. 
	 */
	public String getDescriptor() {
		return fieldDesc;
	}
	
	/**
	 * @return true if the field is static.
	 */
	public boolean isStatic() {
		return isStatic;
	}
	
	/**
	 * @return true if the instruction loads the field value.
	 */
	public boolean isGet() {
		return isGet;
	}

	/**
	 * @return true if the instruction stores the field value.
	 */
	public boolean isPut() {
		return !isGet;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (isGet) {
			b.append("GET");
		} else {
			b.append("PUT");
		}
		if (isStatic) {
			b.append("STATIC");
		} else {
			b.append("FIELD");
		}
		b.append(" ");
		b.append(className);
		b.append(".");
		b.append(fieldName);
		b.append(": ");
		b.append(fieldDesc);
		return b.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		
		FieldAccess fa = (FieldAccess) obj;
		return this.toString().equals(fa.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((fieldDesc == null) ? 0 : fieldDesc.hashCode());
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + (isGet ? 1231 : 1237);
		result = prime * result + (isStatic ? 1231 : 1237);
		return result;
	}
	

}
