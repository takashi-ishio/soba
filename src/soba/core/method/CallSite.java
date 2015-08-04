package soba.core.method;

import soba.core.MethodInfo;

/**
 * This class has information about a call site in a method body.
 */
public class CallSite {

	public enum Kind { STATIC, SPECIAL, VIRTUAL };
	
	private MethodInfo ownerMethod;
	private int instructionIndex;
	private String className;
	private String methodName;
	private String methodDesc;
	private Kind invokeType;
	
	/**
	 * Creates a new <code>CallSite</code> instance.
	 * @param m specifies an owner method of a call site.
	 * @param instIndex specifies an instruction.
	 * @param className is a class name of a callee.
	 * @param methodName is a method name of a callee.
	 * @param methodDesc is a method descriptor of a callee.
	 * @param kind is an invocation kind.
	 */
	public CallSite(MethodInfo m, int instIndex, String className, String methodName, String methodDesc, Kind kind) {
		this.ownerMethod = m;
		this.instructionIndex = instIndex;
		this.className = className;
		this.methodName = methodName;
		this.methodDesc = methodDesc;
		this.invokeType = kind;
	}

	/**
	 * @return a <code>MethodInfo</code> object which is owner of the call site.
	 */
	public MethodInfo getOwnerMethod() {
		return ownerMethod;
	}
	
	/**
	 * @return an index value of the call site in the method body instructions.
	 */
	public int getInstructionIndex() {
		return instructionIndex;
	}
	
	/**
	 * @return true if the method is NOT a virtual method call.
	 * In other words, the method to be invoked is declared as static
	 * or a certain implementation is specified by the invocation,  
	 * e.g. "super.m()". 
	 */
	public boolean isStaticOrSpecial() {
		return invokeType != Kind.VIRTUAL;
	}
	
	/**
	 * @return true if the invocation calls a static method.
	 */
	public boolean isStaticMethod() {
		return invokeType == Kind.STATIC;
	}
	
	/**
	 * @return the class name of the callee.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * @return the method name of the callee.
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * @return the method descriptor of the callee.
	 */
	public String getDescriptor() {
		return methodDesc;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(className);
		sb.append(".");
		sb.append(methodName);
		sb.append(methodDesc);
		sb.append(" called by ");
		sb.append(ownerMethod.toLongString());
		return sb.toString();
	}

}
