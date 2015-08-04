package soba.core.method;


public interface ILocalVariableInfo {

	public boolean isAnonymous();
	public String getName();
	public String getDescriptor();
	public String getGenericsSignature();
	
}
