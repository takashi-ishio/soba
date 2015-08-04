package soba.util;

/**
 * A callback interface for a pair of integers.
 * @author ishio
 *
 */
public interface IntPairProc {

	/**
	 * @return true if you want to continue this loop.
	 */
	public boolean execute(int elem1, int elem2);

}
