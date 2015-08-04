package soba.util;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;


public class IntPairListTest {

	@Test
	public void testAdd() throws Exception {
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		list.add(3, 4);
		list.add(5, 6);
		
		assertThat(list.getFirstValue(0), is(1));
		assertThat(list.getSecondValue(0), is(2));
		assertThat(list.getFirstValue(1), is(3));
		assertThat(list.getSecondValue(1), is(4));
		assertThat(list.getFirstValue(2), is(5));
		assertThat(list.getSecondValue(2), is(6));
		assertThat(list.size(), is(3));
	}

	@Test
	public void testSortByFirstValues() throws Exception {
		IntPairList list = new IntPairList();
		list.add(1, 1); // 1
		list.add(2, 0); // 2
		list.add(3, 0); // 3
		list.add(4, 4); // 4
		list.add(5, 5); // 5
		list.add(6, 3); // 6
		list.add(7, 2); // 7
		list.add(8, 4); // 8
		list.add(9, 1); // 9
		list.add(0, 5); // 0
		
		list.sort();
		
		assertThat(list.getFirstValue(2), is(2));
		assertThat(list.getSecondValue(2), is(0));
		assertThat(list.getFirstValue(4), is(4));
		assertThat(list.getSecondValue(4), is(4));
		assertThat(list.getFirstValue(9), is(9));
		assertThat(list.getSecondValue(9), is(1));
	}


	@Test
	public void testSetValues() throws Exception {
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		list.add(3, 4);
		list.add(5, 6);
		
		list.setFirstValue(1, 10);
		assertThat(list.getFirstValue(0), is(1));
		assertThat(list.getFirstValue(1), is(10));
		assertThat(list.getFirstValue(2), is(5));
		assertThat(list.getSecondValue(0), is(2));
		assertThat(list.getSecondValue(1), is(4));
		assertThat(list.getSecondValue(2), is(6));
		
		list.setSecondValue(2, 27);
		assertThat(list.getFirstValue(0), is(1));
		assertThat(list.getFirstValue(1), is(10));
		assertThat(list.getFirstValue(2), is(5));
		assertThat(list.getSecondValue(0), is(2));
		assertThat(list.getSecondValue(1), is(4));
		assertThat(list.getSecondValue(2), is(27));
	}
	
	@Test
	public void testAddAll() { 
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		list.add(3, 4);
		IntPairList another = new IntPairList(2);
		another.add(5, 6);
		another.add(7, 8);
		
		list.addAll(another);
		assertThat(list.size(), is(4));
		assertThat(list.getFirstValue(0), is(1));
		assertThat(list.getSecondValue(0), is(2));
		assertThat(list.getFirstValue(1), is(3));
		assertThat(list.getSecondValue(1), is(4));
		assertThat(list.getFirstValue(2), is(5));
		assertThat(list.getSecondValue(2), is(6));
		assertThat(list.getFirstValue(3), is(7));
		assertThat(list.getSecondValue(3), is(8));
		assertThat(another.size(), is(2));

		another.addAll(another);
		assertThat(another.size(), is(4));
		assertThat(another.getFirstValue(0), is(5));
		assertThat(another.getSecondValue(0), is(6));
		assertThat(another.getFirstValue(1), is(7));
		assertThat(another.getSecondValue(1), is(8));
		assertThat(another.getFirstValue(2), is(5));
		assertThat(another.getSecondValue(2), is(6));
		assertThat(another.getFirstValue(3), is(7));
		assertThat(another.getSecondValue(3), is(8));
	}
	
	@Test
	public void testFreeze() {
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		list.freeze();
		try {
			list.add(3, 4);
			fail();
		} catch (IntPairList.FrozenListException e) {
		}
		try {
			list.setFirstValue(0, 1);
			fail();
		} catch (IntPairList.FrozenListException e) {
		}
		try {
			list.addAll(list);
			fail();
		} catch (IntPairList.FrozenListException e) {
		}
	}
	
	@Test
	public void testArrayIndexOutOfBounds() {
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		try {
			list.getFirstValue(-1);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			list.getFirstValue(1);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			list.getSecondValue(-1);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			list.getSecondValue(1);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
	}
	
	@Test
	public void testForEach() {
		IntPairList list = new IntPairList(2);
		list.add(1, 2);
		list.add(3, 4);
		list.add(5, 6);
		list.foreach(new IntPairProc() {
			int times = 0;
			@Override
			public boolean execute(int elem1, int elem2) {
				if (times == 0) {
					assertThat(elem1, is(1));
					assertThat(elem2, is(2));
				} else if (times == 1) {
					assertThat(elem1, is(3));
					assertThat(elem2, is(4));
				} else {
					fail();
				}
				times++;
				return times == 1;
			}
		});
	}
}
