package soba.util;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class IntPairUtilTest {

	@Test
	public void testCreateList() {
		IntPairSet set = new IntPairSet();
		set.add(4, 1);
		set.add(3, 2);
		set.add(1, 4);
		set.add(1, 2);
		set.add(4, 1);
		set.add(0, 1);
		set.add(2, 9);
		set.add(3, 2);
		IntPairList list = IntPairUtil.createList(set);
		list.sort();
		assertThat(list.size(), is(6));
		assertThat(list.getFirstValue(0), is(0));
		assertThat(list.getSecondValue(0), is(1));
		assertThat(list.getFirstValue(1), is(1));
		assertThat(list.getSecondValue(1), is(2));
		assertThat(list.getFirstValue(2), is(1));
		assertThat(list.getSecondValue(2), is(4));
		assertThat(list.getFirstValue(3), is(2));
		assertThat(list.getSecondValue(3), is(9));
		assertThat(list.getFirstValue(4), is(3));
		assertThat(list.getSecondValue(4), is(2));
		assertThat(list.getFirstValue(5), is(4));
		assertThat(list.getSecondValue(5), is(1));
	}


	@Test
	public void testCreateList2() {
		IntPairSet set = new IntPairSet();
		set.add(4, 1);
		set.add(3, 2);
		set.add(1, 4);
		set.add(1, 2);
		set.add(4, 1);
		set.add(0, 1);
		set.add(2, 9);
		set.add(3, 2);
		IntPairSet set2 = new IntPairSet();
		set2.add(1, 9);
		set2.add(4, 1);
		
		IntPairList list = IntPairUtil.createList(set, set2);
		list.sort();
		assertThat(list.size(), is(7));
		assertThat(list.getFirstValue(0), is(0));
		assertThat(list.getSecondValue(0), is(1));
		assertThat(list.getFirstValue(1), is(1));
		assertThat(list.getSecondValue(1), is(2));
		assertThat(list.getFirstValue(2), is(1));
		assertThat(list.getSecondValue(2), is(4));
		assertThat(list.getFirstValue(3), is(1));
		assertThat(list.getSecondValue(3), is(9));
		assertThat(list.getFirstValue(4), is(2));
		assertThat(list.getSecondValue(4), is(9));
		assertThat(list.getFirstValue(5), is(3));
		assertThat(list.getSecondValue(5), is(2));
		assertThat(list.getFirstValue(6), is(4));
		assertThat(list.getSecondValue(6), is(1));
	}

}
