package soba.testdata.inheritance2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import soba.testdata.inheritance1.C;
import soba.testdata.inheritance1.D;
import soba.testdata.inheritance1.G;

public class E {

	public static void main(String[] args) {
		new E().exec();
	}
	
	public E() {
		D d = new D(0);
		d.m();
	}
	
	public void testPackagePrivate() {
		F f = new F();
		f.n();
	}


	public void exec() {
		System.err.println("Hello, World!");
		D d = new D();
		d.testPackagePrivate();
		d.testPackagePrivate2();
		d.testPackagePrivate3();
		d.testPackagePrivate4();
	}
	
	private int flag;
	
	/*
	 * Test case:
	 * def1 -> use1, use2
	 * def2 -> use2
	 * VTA judges both use1 and use2 may call D.x(int) and G.x(int)
	 */
	public void testDynamicBinding1() {
		C c = new D(0);   // def1
		c.x(0);           // use1
		if (flag != 0) { 
			c = new G();  // def2
		}
		c.x(1);           // use2
	}
	
	/*
	 * Test case:
	 * def1 -> use1
	 * def2 -> use2
	 * Our VTA implementation judges use1 call D.x(int), use2 calls G.x(int)
	 * This is because our VTA uses a flow-sensitive data-flow analysis 
	 * to distinguish local variable entries.
	 */
	public void testDynamicBinding2() {
		C c = new D(0);   // def1 
		c.x(0);           // use1
		c = new G();      // def2
		c.x(1);           // use2
	}
	
	public void testDynamicBinding3() {
		D d1 = new D(0);   // def1 
		d1.y(1);           // use1 -- this invokes C.y() because D does not override C.y().
		C d2 = new D(0);   // def2 
		d2.y(1);           // use2
	}

	public void testDynamicBinding4(C c) {
		if (c == null) {
			c = new D(0);
		}
		testDynamicBinding5(c);
		c.x(0);
	}

	public void testDynamicBinding5(C c) {
		if (c == null) {
			c = new G();
		}
		testDynamicBinding4(c);
		c.x(1);
	}
	
	public void testDynamicBinding6(L l) {
		l.getC().x(0);  // There are no implementation of getC()
	}

	public void testDynamicBinding7(boolean b) {
		List<String> list;
		if (b) {
			list = new ArrayList<>();
		} else {
			list = new LinkedList<>();
		}
		int size =list.size();
	}
}
