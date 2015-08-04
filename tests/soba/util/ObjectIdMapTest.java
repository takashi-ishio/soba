package soba.util;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ObjectIdMapTest {

	@Test
	public void testObjectIdMap() {
		ObjectIdMap<String> idMap = new ObjectIdMap<String>();
		int idABC = idMap.getId("abc");
		int idAB = idMap.getId("ab");
		int idABC2 = idMap.getId("abc");
		int idD = idMap.getId("d");
		assertThat(idMap.size(), is(3));
		assertThat(idABC, is(0));
		assertThat(idABC2, is(0));
		assertThat(idAB, is(1));
		assertThat(idD, is(2));
		assertThat(idMap.getItem(idABC), is("abc"));
		assertThat(idMap.getItem(idAB), is("ab"));
		assertThat(idMap.getItem(idD), is("d"));
		assertThat(idMap.getItem(-1), is(nullValue()));
		assertThat(idMap.getItem(4), is(nullValue()));
	}
	
	@Test
	public void testAdd() { 
		ObjectIdMap<String> idMap = new ObjectIdMap<String>();
		idMap.add("abc");
		idMap.add("ab");
		idMap.add("ab");
		idMap.add("abc");
		assertThat(idMap.size(), is(2));
		assertThat(idMap.getItem(0), is("abc"));
		assertThat(idMap.getItem(1), is("ab"));
	}

	@Test
	public void testFreeze() { 
		ObjectIdMap<String> idMap = new ObjectIdMap<String>();
		idMap.add("abc");
		idMap.add("ab");
		idMap.freeze();
		try {
			idMap.add("abc"); // ignored already registerd item
			idMap.add("xyz"); // throws an exception
			fail();
		} catch (ObjectIdMap.FrozenMapException e) {
		}
		assertThat(idMap.getId("ab"), is(1));
		assertThat(idMap.getItem(0), is("abc"));
		assertThat(idMap.getItem(3), is(nullValue()));
	}

}
