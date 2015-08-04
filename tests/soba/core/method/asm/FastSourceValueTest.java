package soba.core.method.asm;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

//import org.junit.Assert;
import org.junit.Test;

import soba.core.method.asm.FastSourceValue;
import soba.util.UtilForAssertThat;

public class FastSourceValueTest {

	@Test
	public void testConstructorWithoutInstruction() {
		FastSourceValue value1 = new FastSourceValue(1);
		assertThat(value1.getSize(), is(1));
		assertThat(value1.getInstructions().length, is(0));

		FastSourceValue value2 = new FastSourceValue(2);
		assertThat(value2.getSize(), is(2));
		assertThat(value2.getInstructions().length, is(0));
	}
	
	@Test
	public void testConstructorWithSingleInstruction() {
		FastSourceValue value1 = new FastSourceValue(1, 2);
		assertThat(value1.getSize(), is(1));
		assertThat(value1.getInstructions().length, is(1));
		assertThat(value1.getInstructions()[0], is(2));
	}
	
	@Test
	public void testConstructorWithArray() {
		FastSourceValue value1 = new FastSourceValue(2, new int[] { 1, 4, 9 });
		assertThat(value1.getSize(), is(2));
		assertThat(value1.getInstructions().length, is(3));
		assertThat(value1.getInstructions()[0], is(1));
		assertThat(value1.getInstructions()[1], is(4));
		assertThat(value1.getInstructions()[2], is(9));
	}
	
	@Test
	public void testMerge() {
		FastSourceValue value123 = new FastSourceValue(1, new int[] {1, 2, 3});
		FastSourceValue value456 = new FastSourceValue(1, new int[] {4, 5, 6});
		FastSourceValue value135 = new FastSourceValue(1, new int[] {1, 3, 5});
		FastSourceValue value246 = new FastSourceValue(1, new int[] {2, 4, 6});
		FastSourceValue valueNULL = new FastSourceValue(1, new int[0]);
		FastSourceValue value123Size2 = new FastSourceValue(2, new int[] {1, 2, 3});
		
		FastSourceValue value123456concat = new FastSourceValue(value123, value456);
		Integer[] instructions123456 = UtilForAssertThat.asIntegerArray(value123456concat.getInstructions());
		assertThat(instructions123456, is(arrayContainingInAnyOrder(1, 2, 3, 4, 5, 6)));
		
		FastSourceValue value123456anotherConcat = new FastSourceValue(value456, value123);
		Integer[] instructions123456another = UtilForAssertThat.asIntegerArray(value123456anotherConcat.getInstructions());
		assertThat(instructions123456another, is(arrayContainingInAnyOrder(1, 2, 3, 4, 5, 6)));

		FastSourceValue value123456mix = new FastSourceValue(value135, value246);
		Integer[] instructions123456mix = UtilForAssertThat.asIntegerArray(value123456mix.getInstructions());
		assertThat(instructions123456mix, is(arrayContainingInAnyOrder(1, 2, 3, 4, 5, 6)));

		FastSourceValue value123456anotherMix = new FastSourceValue(value246, value135);
		Integer[] instructions123456anotherMix = UtilForAssertThat.asIntegerArray(value123456anotherMix.getInstructions());
		assertThat(instructions123456anotherMix, is(arrayContainingInAnyOrder(1, 2, 3, 4, 5, 6)));

		FastSourceValue value123unchanged = new FastSourceValue(value123, valueNULL);
		Integer[] instructions123 = UtilForAssertThat.asIntegerArray(value123unchanged.getInstructions());
		assertThat(instructions123, is(arrayContainingInAnyOrder(1, 2, 3)));

		FastSourceValue value246unchanged = new FastSourceValue(valueNULL, value246);
		Integer[] instructions246 = UtilForAssertThat.asIntegerArray(value246unchanged.getInstructions());
		assertThat(instructions246, is(arrayContainingInAnyOrder(2, 4, 6)));
		
		FastSourceValue valueConcatNull = new FastSourceValue(valueNULL, valueNULL);
		Integer[] instructionsNull = UtilForAssertThat.asIntegerArray(valueConcatNull.getInstructions());
		assertThat(instructionsNull, is(emptyArray()));
		
		FastSourceValue value1235 = new FastSourceValue(value123, value135);
		Integer[] instructions1235 = UtilForAssertThat.asIntegerArray(value1235.getInstructions());
		assertThat(instructions1235, is(arrayContainingInAnyOrder(1, 2, 3, 5)));

		FastSourceValue value1235another = new FastSourceValue(value135, value123);
		Integer[] instructions1235another = UtilForAssertThat.asIntegerArray(value1235another.getInstructions());
		assertThat(instructions1235another, is(arrayContainingInAnyOrder(1, 2, 3 ,5)));

		FastSourceValue value2456 = new FastSourceValue(value246, value456);
		Integer[] instructions2456 = UtilForAssertThat.asIntegerArray(value2456.getInstructions());
		assertThat(instructions2456, is(arrayContainingInAnyOrder(2, 4, 5, 6)));

		FastSourceValue value2456another = new FastSourceValue(value456, value246);
		Integer[] instructions2456another = UtilForAssertThat.asIntegerArray(value2456another.getInstructions());
		assertThat(instructions2456another, is(arrayContainingInAnyOrder(2, 4, 5, 6)));
		
		FastSourceValue value123differentSize = new FastSourceValue(value123, value123Size2);
		Integer[] instructions123different = UtilForAssertThat.asIntegerArray(value123differentSize.getInstructions());
		assertThat(instructions123different, is(arrayContainingInAnyOrder(1, 2, 3)));
		assertThat(value123differentSize.getSize(), is(1));

		FastSourceValue value123differentSizeAnother = new FastSourceValue(value123Size2, value123);
		Integer[] instructions123differentAnother = UtilForAssertThat.asIntegerArray(value123differentSizeAnother.getInstructions());
		assertThat(instructions123differentAnother, is(arrayContainingInAnyOrder(1, 2, 3)));
		assertThat(value123differentSizeAnother.getSize(), is(1));
	}

	@Test
	public void testContainsAll() {
		FastSourceValue value123 = new FastSourceValue(1, new int[] {1, 2, 3});
		FastSourceValue value246 = new FastSourceValue(1, new int[] {2, 4, 6});
		FastSourceValue valueNULL = new FastSourceValue(1, new int[0]);
		FastSourceValue value0 = new FastSourceValue(1, 0);
		FastSourceValue value1 = new FastSourceValue(1, 1);
		FastSourceValue value2 = new FastSourceValue(1, 2);
		FastSourceValue value3 = new FastSourceValue(1, 3);
		FastSourceValue value4 = new FastSourceValue(1, 4);
		FastSourceValue value12 = new FastSourceValue(1, new int[] {1, 2});
		FastSourceValue value23 = new FastSourceValue(1, new int[] {2, 3});
		FastSourceValue value45 = new FastSourceValue(1, new int[] {4, 5});
		FastSourceValue value46 = new FastSourceValue(1, new int[] {4, 6});
		FastSourceValue value1234 = new FastSourceValue(1, new int[] {1, 2, 3, 4});
		FastSourceValue value24 = new FastSourceValue(1, new int[] {2, 4});
		
		assertThat(value123.containsAll(valueNULL), is(true));
		assertThat(value123.containsAll(value123), is(true));
		assertThat(value123.containsAll(value1), is(true));
		assertThat(value123.containsAll(value2), is(true));
		assertThat(value123.containsAll(value3), is(true));
		assertThat(value123.containsAll(value12), is(true));
		assertThat(value123.containsAll(value23), is(true));
		assertThat(value123.containsAll(value0), is(false));
		assertThat(value123.containsAll(value4), is(false));
		assertThat(value123.containsAll(value246), is(false));
		assertThat(value123.containsAll(value45), is(false));
		assertThat(value123.containsAll(value46), is(false));
		assertThat(value123.containsAll(value1234), is(false));
		assertThat(value123.containsAll(value24), is(false));
		
		assertThat(valueNULL.containsAll(valueNULL), is(true));
		assertThat(valueNULL.containsAll(value1), is(false));
		assertThat(valueNULL.containsAll(value123), is(false));
		
		assertThat(value246.containsAll(value46), is(true));
		assertThat(value246.containsAll(value4), is(true));
		assertThat(value246.containsAll(value24), is(true));
		assertThat(value246.containsAll(value123), is(false));
	}

	@Test
	public void testEquals() {
		int[] v123 = new int[] {1, 2, 3};
		FastSourceValue value123 = new FastSourceValue(1, v123);
		FastSourceValue value123another = new FastSourceValue(1, v123);
		FastSourceValue value123differentSize = new FastSourceValue(2, v123);
		FastSourceValue value123differentArray = new FastSourceValue(1, new int[] {1, 2, 3});
		FastSourceValue value123differentSizeAndArray = new FastSourceValue(2, new int[] {1, 2, 3});
		
		assertThat(value123.equals(value123another), is(true));
		assertThat(value123.equals(value123differentSize), is(false));
		assertThat(value123.equals(value123differentArray), is(true));
		assertThat(value123.equals(value123differentSizeAndArray), is(false));
	}
}
