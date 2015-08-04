package soba.testdata.inheritance1;

import soba.testdata.inheritance2.F;
import soba.testdata.inheritance2.H;

public class D extends C implements I, K {

	
	public D() {
		this(K.x);
		System.err.println("D.<init>");
	}
	
	public D(int x) {
		super(x);
		System.err.println("D.<init>(int)");
	}
	
	public void m() {
		super.m();
		System.err.println("D.m");
	}
	
	public void testPackagePrivate() {
		C c = new C(0);
		c.n();
	}

	public void testPackagePrivate2() {
		C c = new F();
		c.n();
	}

	public void testPackagePrivate3() {
		C c = new G();
		c.n();
	}

	public void testPackagePrivate4() {
		C c = new H();
		c.n();
	}

	public void n() {
		System.err.println("D.n()");
	}
	
	public int example(int i, long l, double d, String s) {
		return i;
	}
	
	public void x(int t) {
		System.out.println("D.x(int)");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
