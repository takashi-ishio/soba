package soba.util;

import java.util.EmptyStackException;

/**
 * This class represents a integer stack.
 */
public class IntStack {

	private int count = 0;
	private int[] values;

	/**
	 * Creates a new <code>IntStack</code> with the default size.
	 */
	public IntStack() {
		this(1024);
	}
	
	/**
	 * Creates a new <code>IntStack</code> with a specified size.
	 * @param capacity is a size of the stack.
	 */
	public IntStack(int capacity) {
		count = 0;
		values = new int[capacity];
	}
	
	/**
	 * @return true if the stack is empty.
	 */
	public boolean isEmpty() {
		return count == 0;
	}
	
	/**
	 * Pushes a value to the top of the stack.
	 * @param value
	 */
	public void push(int value) {
		if (count >= values.length) {
			growUp();
		}
		values[count] = value;
		count++;
	}
	
	/**
	 * Gets a value from the top of the stack.
	 * The value is deleted from the stack.
	 * @return a top value of the stack.
	 */
	public int pop() {
		if (count == 0) {
			throw new EmptyStackException();
		}
		count--;
		return values[count];
	}
	
	/**
	 * Gets a top value of the stack.
	 * The value is not deleted from the stack.
	 * @return a top value of the stack.
	 */
	public int peek() {
		if (count == 0) {
			throw new EmptyStackException();
		}
		return values[count-1];
	}
	
	/**
	 * @param value
	 * @return true if the value is contained in the stack.
	 */
	public boolean contains(int value) {
		for (int i=0; i<count; ++i) {
			if (values[i] == value) return true;
		}
		return false;
	}

	private void growUp() {
		int[] newValues = new int[values.length * 2];
		for (int i=0; i<count; ++i) {
			newValues[i] = values[i];
		}
		values = newValues;
	}
	
	/**
	 * @return the number of elements in the stack.
	 */
	public int size() {
		return count;
	}
}
