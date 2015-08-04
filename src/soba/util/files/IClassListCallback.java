package soba.util.files;

import java.io.IOException;
import java.io.InputStream;

public interface IClassListCallback {

	
	/**
	 * @param name is a string representing the data. 
	 * @return The implementation returns true if the implementation 
	 * want to process the data.
	 * The implementation may return false to skip the data. 
	 */
	public boolean isTarget(String name);
	
	public void process(String name, InputStream stream) throws IOException;
	
	/**
	 * @param name represents the data.
	 * @param e is an exception occurred during the process.
	 * @return The implementation returns true if the implementation
	 * want to stop the process as soon as possible.
	 */
	public boolean reportError(String name, Exception e);
	

}
