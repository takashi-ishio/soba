package soba.testdata.inheritance2;

import soba.testdata.inheritance1.C;

public class F extends C {

	public F() {
		super(0);
	}
	
	/**
	 *  This method declaration does not override C.n()
	 *  because C.n() is package-private.
	 */
	void n() {
		System.err.println("F.n()");
	}
	
	public void o() {
		System.err.println("F.o()");
	}
	
	public void k() {
		String[][][] array = new String[10][20][30];
		System.err.println(array.toString());
		String[] another = new String[10];
		System.err.println(another.toString());
	}
}
