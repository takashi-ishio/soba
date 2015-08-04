package soba.util.files;

public interface IClassList {

	/**
	 * Start a file enumeration process.
	 * @param c receives call back method calls from the enumerator.
	 */
	public void process(IClassListCallback c);
	
	
	public String getLabel();
	
}
