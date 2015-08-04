package soba.core;

import org.objectweb.asm.tree.FieldNode;

import soba.core.signature.TypeResolver;

/**
 * This class represents a field variable.
 */
public class FieldInfo {

	private ClassInfo owner;
	private FieldNode fieldNode;
	private String typeName;
	
	/**
	 * Creates a new <code>FieldInfo</code> instance.
	 * @param owner is a <code>ClassInfo</code> object which declares this field.
	 * @param fieldNode
	 */
	public FieldInfo(ClassInfo owner, FieldNode fieldNode) {
		this.owner = owner;
		this.fieldNode = fieldNode;
		this.typeName = null;
	}
	
	/**
	 * @return the package name who has the field.
	 */
	public String getPackageName() {
		return owner.getPackageName();
	}
	
	/**
	 * @return the class name who has the field.
	 */
	public String getClassName() {
		return owner.getClassName();
	}
	
	/**
	 * @return the field name.
	 */
	public String getFieldName() {
		return fieldNode.name;
	}
	
	/**
	 * @return the descriptor of the field.
	 */
	public String getDescriptor() {
		return fieldNode.desc;
	}
	
	/**
	 * @return the type name of the field.
	 */
	public String getFieldTypeName() {
		if (typeName == null) {
			if (fieldNode.signature != null) {
				typeName = TypeResolver.getTypeName(fieldNode.signature);
			} else {
				typeName = TypeResolver.getTypeName(fieldNode.desc);
			}
		}
		return typeName;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClassName());
		builder.append(".");
		builder.append(getFieldName());
		builder.append(": ");
		builder.append(getFieldTypeName());
		return builder.toString();
	}
	
}
