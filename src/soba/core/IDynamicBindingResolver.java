package soba.core;

import soba.core.method.CallSite;

public interface IDynamicBindingResolver {

	/**
	 * Resolves the dynamic binding of a method invocation.
	 * @param cs is a method invocation.
	 * @return an array of the methods which may be invoked.
	 */
	public MethodInfo[] resolveCall(CallSite cs);
}
