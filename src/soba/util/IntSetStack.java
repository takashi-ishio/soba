package soba.util;

/**
 * <code>IntSetStack</code> is a stack in which int values can be stored.
 * The stack cannot contain two instances of the same value;
 * for example, "aIntSetStack.push(0); aIntSetStack.push(0);" 
 * causes a DuplicatedValueException.
 * Instead of the set constraint, this stack 
 * provides O(1) implementation of contains(int).
 */
public class IntSetStack extends IntStack {

	private int max;
	private static final int DIV = 31;
	private int[] elements;
	private boolean ignoreDuplicatedElement;
	 
	private static int elementIndex(int value) {
		return value/DIV;
	}

	private static int bitMask(int value) {
		int b = value%DIV;
		return 1 << b;
	}

	/**
	 * Creates a new <code>IntSetStack</code> instance.
	 * @param maxValue is a maximum size of the stack.
	 */
	public IntSetStack(int maxValue) {
		super();
		max = maxValue;
		elements = new int[elementIndex(maxValue)+1];
	}
	
	/**
	 * Creates a new <code>IntSetStack</code> instance with a specified size.
	 * @param maxValue is a maximum size of the stack.
	 * @param capacity is a size of the stack. 
	 */
	public IntSetStack(int maxValue, int capacity) {
		super(capacity);
		elements = new int[elementIndex(maxValue)+1];
	}
	
	public void setIgnoreDuplicatedElement(boolean ignore) {
		ignoreDuplicatedElement = ignore;
	}
	
	/**
	 * O(1) implementation 
	 */
	@Override
	public boolean contains(int value) {
		int bit = elements[ elementIndex(value) ];
		return ((bit & bitMask(value)) != 0);
	}
	
	/** {@inheritDoc} */
	@Override
	public void push(int value) {
		if (max < value) throw new InvalidElementException(max, value);
		int index = elementIndex(value);
		int bit = elements[ index ];
		int bitMask = bitMask(value);
		if ((bit & bitMask) == 0) {
			super.push(value);
			elements[ index ] |= bitMask;
		} else {
			if (!ignoreDuplicatedElement) {
				throw new DuplicatedElementException();
			}
		}
		assert (elements[index] & bitMask) != 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public int pop() {
		int value = super.pop();
		int index = elementIndex(value);
		int bitMask = bitMask(value);
		elements[index] ^= bitMask;
		assert (elements[index] & bitMask) == 0;
		return value;
	}

	public class InvalidElementException extends RuntimeException {

		private static final long serialVersionUID = -8544940855615952576L;

		private int max;
		private int value;
		
		public InvalidElementException(int max, int value) {
			this.max = max;
			this.value = value;
		}
		
		@Override
		public String getMessage() {
			return "The given value cannot be stored into the stack. MAX=" + 
			        Integer.toString(max) + ", VALUE=" + Integer.toString(value);
		}
		
		public int getValue() {
			return value;
		}
		
		public int getMax() {
			return max;
		}
	}

	public class DuplicatedElementException extends RuntimeException {

		private static final long serialVersionUID = -3896583429225358307L;

		public DuplicatedElementException() {
		}
	}

}
