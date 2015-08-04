package soba.testdata.inheritance2;

import soba.testdata.inheritance1.D;

public class H extends D {

	protected int x;
	
	public H() {
		p(1);
		q(0.1);
	}
	
	@Override
	public void n() {
		System.err.println("H.n()");
	}
	
	protected void p(int x) {
		System.out.println("C.p(int)");
	}
	
	private void q(double d) {
		System.out.println("C.q(double)");
	}

}
