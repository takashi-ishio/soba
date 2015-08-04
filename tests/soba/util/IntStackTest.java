package soba.util;

import java.util.EmptyStackException;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;


public class IntStackTest {

	@Test
	public void testStack() throws Exception {
		IntStack stack = new IntStack(3);
		assertThat(stack.isEmpty(), is(true));
		stack.push(0);
		assertThat(stack.peek(), is(0));
		assertThat(stack.isEmpty(), is(false));
		stack.push(1);
		assertThat(stack.peek(), is(1));
		assertThat(stack.isEmpty(), is(false));
		stack.push(2);
		assertThat(stack.peek(), is(2));
		assertThat(stack.isEmpty(), is(false));
		
		assertThat(stack.pop(), is(2));
		assertThat(stack.peek(), is(1));
		assertThat(stack.pop(), is(1));
		assertThat(stack.pop(), is(0));
		assertThat(stack.isEmpty(), is(true));
		try {
			stack.pop();
			fail();
		} catch (EmptyStackException e) {
		}
		try {
			stack.peek();
			fail();
		} catch (EmptyStackException e) {
		}
		
		stack.push(0);
		stack.push(1);
		stack.push(2);
		stack.push(3);
		stack.push(4);
		stack.push(5);
		stack.push(6);
		assertThat(stack.pop(), is(6));
		assertThat(stack.contains(3), is(true));
		assertThat(stack.pop(), is(5));
		assertThat(stack.pop(), is(4));
		assertThat(stack.contains(3), is(true));
		assertThat(stack.pop(), is(3));
		assertThat(stack.contains(3), is(false));
		assertThat(stack.pop(), is(2));
		assertThat(stack.pop(), is(1));
		assertThat(stack.isEmpty(), is(false));
		assertThat(stack.pop(), is(0));
		assertThat(stack.isEmpty(), is(true));
	}
}
