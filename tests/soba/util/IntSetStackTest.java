package soba.util;

import java.util.EmptyStackException;

import org.junit.Assert;
import org.junit.Test;

import soba.util.IntSetStack.DuplicatedElementException;
import soba.util.IntSetStack.InvalidElementException;


public class IntSetStackTest {

	@Test
	public void testStack() throws Exception {
		IntSetStack stack = new IntSetStack(3);
		Assert.assertTrue(stack.isEmpty());
		Assert.assertFalse(stack.contains(0));
		Assert.assertFalse(stack.contains(1));
		Assert.assertFalse(stack.contains(2));
		
		stack.push(0);
		Assert.assertEquals(0, stack.peek());
		Assert.assertFalse(stack.isEmpty());
		Assert.assertTrue(stack.contains(0));
		Assert.assertFalse(stack.contains(1));
		Assert.assertFalse(stack.contains(2));
		
		stack.push(1);
		Assert.assertEquals(1, stack.peek());
		Assert.assertFalse(stack.isEmpty());
		Assert.assertTrue(stack.contains(0));
		Assert.assertTrue(stack.contains(1));
		Assert.assertFalse(stack.contains(2));
		
		stack.push(2);
		Assert.assertEquals(2, stack.peek());
		Assert.assertFalse(stack.isEmpty());
		Assert.assertTrue(stack.contains(0));
		Assert.assertTrue(stack.contains(1));
		Assert.assertTrue(stack.contains(2));
		
		Assert.assertEquals(2, stack.pop());
		Assert.assertEquals(1, stack.peek());
		Assert.assertTrue(stack.contains(0));
		Assert.assertTrue(stack.contains(1));
		Assert.assertFalse(stack.contains(2));

		Assert.assertEquals(1, stack.pop());
		Assert.assertEquals(0, stack.pop());
		Assert.assertTrue(stack.isEmpty());

		Assert.assertFalse(stack.contains(0));
		Assert.assertFalse(stack.contains(1));
		Assert.assertFalse(stack.contains(2));

		try {
			stack.pop();
			Assert.fail();
		} catch (EmptyStackException e) {
		}
		
		try {
			stack.peek();
			Assert.fail();
		} catch (EmptyStackException e) {
		}
	}
	
	@Test
	public void testStackContains() {
		IntSetStack stack = new IntSetStack(128);
		try {
			stack.push(64);
			stack.push(96);
			stack.push(127);
			stack.push(128);
			stack.push(129);
			Assert.fail();
		} catch (InvalidElementException e) {
			Assert.assertEquals(129, e.getValue());
		}
		
		Assert.assertTrue(stack.contains(64));
		Assert.assertTrue(stack.contains(96));
		Assert.assertTrue(stack.contains(127));
		Assert.assertTrue(stack.contains(128));
		Assert.assertFalse(stack.contains(63));
		Assert.assertFalse(stack.contains(65));
		Assert.assertFalse(stack.contains(95));
		Assert.assertFalse(stack.contains(97));
		Assert.assertFalse(stack.contains(126));
		
		try {
			stack.push(64);
			Assert.fail();
		} catch (DuplicatedElementException e) {
		}
	}
	
	@Test
	public void testStackIgnoreDuplicatedElement() {
		IntSetStack stack = new IntSetStack(8);
		stack.setIgnoreDuplicatedElement(true);
		stack.push(1);
		stack.push(2);
		stack.push(3);
		stack.push(4);
		stack.push(2);
		stack.push(4);
		stack.push(1);
		stack.push(3);
		stack.push(0);
		Assert.assertEquals(0, stack.pop());
		Assert.assertEquals(4, stack.pop());
		Assert.assertEquals(3, stack.pop());
		Assert.assertEquals(2, stack.pop());
		Assert.assertEquals(1, stack.pop());
		Assert.assertTrue(stack.isEmpty());
		stack.push(1);
		stack.push(2);
		stack.push(3);
		stack.push(4);
		Assert.assertEquals(4, stack.pop());
		Assert.assertEquals(3, stack.pop());
		Assert.assertEquals(2, stack.pop());
		Assert.assertEquals(1, stack.pop());
		Assert.assertTrue(stack.isEmpty());
	}
}
