package soba.testdata;

public class StatementUnitsData {

	public void f1(boolean b) {
		int i = b ? 1: 0;
		System.out.println(i);
	}
	
	public void f2(boolean b) {
		int i = b ? 1: 0;
		System.out.println(i == 0 ? true : false);
	}

	public void f3(boolean b1, boolean b2) {
		boolean i = b1 && b2 ? b2 : (b1 || b2);
		System.out.println(i);
	}

	public void if1(boolean b1, boolean b2) {
		if (b1 && b2) {
			System.out.println(b1);
		}
	}

	public void if1nest(boolean b1, boolean b2) {
		if (b1) {
			if (b2) {
				System.out.println(b1);
			}
		}
	}

	public void if2(boolean b1, boolean b2) {
		if (b1 || b2) {
			System.out.println(b1);
		}
	}

	public void if3(boolean b1, boolean b2) {
		if ((b1 && !b2) || (!b1 && b2)) {
			System.out.println(b1);
		}
	}

	public void if4(boolean b1, boolean b2) {
		if ((b1 || !b2) && (!b1 || b2)) {
			System.out.println(b1);
		}
	}

	/**
	 * This method has exactly the same bytecode as if4. (in Eclipse 3.8.0)
	 */
	public void if4nest(boolean b1, boolean b2) {
		if (b1 || !b2) { if (!b1 || b2) {
				System.out.println(b1);
		} }
	}

	public void g1(boolean b1, boolean b2) {
		if ((b1 || b2) ? b1 : b2) {
			System.out.println(b1);
		}
	}

	public void g1nest(boolean b1, boolean b2) {
		if ((b1 || b2)) {
			if (b1) {
				System.out.println(b1);
			}
		} else {
			if (b2) {
				System.out.println(b1);
			}
		}
	}

	public void g2(boolean b1, boolean b2, boolean b3) {
		boolean b = !(b1 && b2) || b3;
		System.out.println(b);
	}

	public void h1(boolean b1, boolean b2, boolean b3) {
		boolean b;
		if (b1) {
			b = b2;
		} else {
			b = b3;
		}
		System.out.println(b);
	}

	public void h2(boolean b1, boolean b2, boolean b3, boolean b4) {
		boolean b;
		if (b1 && b2) {
			b = b3;
		} else {
			b = b4;
		}
		System.out.println(b);
	}

	public void h3(int x, int y, int z, boolean b) {
		if ((x = (b ? y : z)) == 0 ) {
			System.out.println(b);
		}
	}

	public void i1(Object obj, boolean x, boolean y) {
		boolean b = (x && y) ? (obj == null) : false;
		System.out.println(b);
	}

	public void i2(Object obj) {
		boolean b = (obj == null);
		System.out.println(b);
	}

	public interface I {
		public void method(int x, int y);
	}
}
