package soba.util;

public class UtilForAssertThat {	

	public static Integer[] asIntegerArray(int[] array) {
		Integer[] result = new Integer[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
}
