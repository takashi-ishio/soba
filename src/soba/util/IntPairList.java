package soba.util;

import java.util.Arrays;

/**
 * This class represents a list of integer pairs.
 */
public class IntPairList {

	private int count;
	private long[] values;
	private boolean frozen;

	/**
	 * Creates a new <code>IntPairList</code> instance with the default size.
	 */
	public IntPairList() {
		this(1024);
	}

	/**
	 * @param capacity specifies the list size.
	 * Creates a new <code>IntPairList</code> instance with the specified size.
	 */
	public IntPairList(int capacity) {
		values = new long[capacity]; 
		count = 0;
	}
	
	/**
	 * @return the number of elements.
	 */
	public int size() {
		return count; 
	}
	
	private long compose(int elem1, int elem2) {
		return (((long)elem1) << 32) | elem2;
	}

	/**
	 * @param index specifies a pair.
	 * @return a first value of the specified pair.
	 */
	public int getFirstValue(int index) {
		if (index < 0 || index >= count) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return (int)(values[index] >> 32);
	}

	/**
	 * @param index specifies a pair.
	 * @return a second value of the specified pair.
	 */
	public int getSecondValue(int index) {
		if (index < 0 || index >= count) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return (int)(values[index] & 0xFFFFFFFF);
	}

	/**
	 * Adds a pair of integers.
	 * @param elem1 specifies a first value.
	 * @param elem2 specifies a second value.
	 */
	public void add(int elem1, int elem2) {
		if (!frozen) {
			if (values.length == count) growUp();
			values[count] = compose(elem1, elem2);
			count++;
		} else {
			throw new FrozenListException();
		}
	}
	
	/**
	 * Adds all the pairs of another list.
	 * @param another is a <code>IntPairList</code> object.
	 */
	public void addAll(IntPairList another) {
		if (!frozen) {
			int anotherSize = another.size();
			for (int i=0; i<anotherSize; ++i) {
				if (values.length == count) growUp();
				values[count] = another.values[i];
				count++;
			}
		} else {
			throw new FrozenListException();
		}
	}

	private void growUp() {
		long[] newValues = new long[values.length * 2];
		for (int i=0; i<count; ++i) {
			newValues[i] = values[i];
		}
		values = newValues;
	}
	
	/**
	 * Sets a first value of a pair at the specified position.
	 * @param index specifies a position in the list.
	 * @param first is a stored value.
	 */
	public void setFirstValue(int index, int first) {
		if (!frozen) {
			if ((index < 0) || (count <= index)) {
				throw new ArrayIndexOutOfBoundsException();
			} else {
				int second = getSecondValue(index);
				values[index] = compose(first, second);
			}
		} else {
			throw new FrozenListException();
		}
	}
	
	/**
	 * Sets a second value of a pair at the specified position.
	 * @param index specifies a position in the list.
	 * @param second is a stored value.
	 */
	public void setSecondValue(int index, int second) {
		if (!frozen) {
			if ((index < 0) || (count <= index)) {
				throw new ArrayIndexOutOfBoundsException();
			} else {
				int first = getFirstValue(index);
				values[index] = compose(first, second);
			}
		} else {
			throw new FrozenListException();
		}
	}
	
	/**
	 * Sorts a list by the order of the composed values.
	 */
	public void sort() {
		Arrays.sort(values, 0, count);
	}
	
	/**
	 * Executes a procedure for each element.
	 * @param proc
	 */
	public void foreach(IntPairProc proc) {
		boolean cont = true;
		for (int i=0; cont && (i<count); ++i) {
			cont = proc.execute(getFirstValue(i), getSecondValue(i));
		}
	}
	
	
	/**
	 * This method freezes the list and releases 
	 * unnecessary memory buffer for additional elements.
	 * If you add a new pair of integers, 
	 * this list throws an exception.
	 */
	public void freeze() {
		frozen = true;
	}
	
	public static class FrozenListException extends RuntimeException {
		private static final long serialVersionUID = 1519361503126979153L;
	}
	
}
