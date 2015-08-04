package soba.util;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class IntPairSetTest {

	@Test
	public void testIntPairSet() {
		IntPairSet set = new IntPairSet();
		assertThat(set.size(), is(0));
		set.add(0, 1);
		assertThat(set.size(), is(1));
		set.add(3, 2);
		assertThat(set.size(), is(2));
		assertThat(set.contains(0, 1), is(true));
		assertThat(set.contains(3, 2), is(true));
		assertThat(set.contains(0, 2), is(false));
		set.add(1, 2);
		assertThat(set.size(), is(3));
		set.add(0, 1);
		assertThat(set.size(), is(3));
		assertThat(set.contains(0, 1), is(true));
		assertThat(set.contains(1, 2), is(true));
		assertThat(set.contains(3, 2), is(true));
	}

	private boolean visited01 = false;
	private boolean visited12 = false;
	private boolean visited14 = false;
	private boolean visited32 = false;
	private boolean visited41 = false;
	
	@Test
	public void testForEach() {
		IntPairSet set = new IntPairSet();
		set.add(4, 1);
		set.add(3, 2);
		set.add(1, 4);
		set.add(1, 2);
		set.add(0, 1);
		set.foreach(new IntPairProc() {
			
			@Override
			public boolean execute(int elem1, int elem2) {
				switch (elem1) {
				case 0:	
					assertThat(elem2, is(1));
					assertThat(visited01, is(false));
					visited01 = true;
					break;
				case 1:
					if (elem2 == 2) {
						assertThat(visited12, is(false));
						visited12 = true;
					} else if (elem2 == 4) {
						assertThat(visited14, is(false));
						visited14 = true;
					} else {
						fail();
					}
					break;
				case 3:
					assertThat(elem2, is(2));
					assertThat(visited32, is(false));
					visited32 = true;
					break;
				case 4:
					assertThat(elem2, is(1));
					assertThat(visited41, is(false));
					visited41 = true;
					break;
				default:
					fail();
				}
				return true;
			}
		});
		assertThat(visited01, is(true));
		assertThat(visited12, is(true));
		assertThat(visited14, is(true));
		assertThat(visited32, is(true));
		assertThat(visited41, is(true));
	}
	
}
