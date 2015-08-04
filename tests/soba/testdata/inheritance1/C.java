package soba.testdata.inheritance1;

import soba.testdata.inheritance2.F;

public class C {

	protected int x;
	
	public static void main(String[] args) {
		System.err.println(J.x);
		F f = new F();
		C c = f;
		c.o();
		f.o();
	}
	
	public C(int x) {
		System.err.println("C.<init>");
		q(0.0);
	}

	protected void m() {
		System.err.println("C.m");
	}
	
	void n() {
		System.err.println("C.n()");
	}
	
	void o() {
		System.err.println("C.o()");
	}
	
	protected void p(int x) {
		System.out.println("C.p(int)");
	}
	
	private void q(double d) {
		System.out.println("C.q(double)");
	}
	
	public static void novariables() {
		
	}
	
	public void x(int t) {
		System.out.println("C.x(int)");
	}
	
	public void y(int t) {
		System.out.println("C.y(int)");
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
