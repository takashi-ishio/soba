package soba.core;

public interface IClassFilter {

	/**
	 * This method determines whether JavaProgram load the class.
	 * @param dataName indicates a class file name (maybe in a zip file).
	 * @return true to make JavaProgram load the class.
	 */
	public boolean loadClass(String dataName);
	
	/**
	 * This method determines whether JavaProgram registers the class 
	 * to a class hierarchy object. 
	 * @param c is a loaded ClassInfo object.
	 * @return true to make JavaProgram include the ClassInfo object.
	 */
	public boolean acceptClass(ClassInfo c);
}
