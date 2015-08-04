package soba.testdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class itself has no functionality. 
 * The code is used only for testing.
 * @author ishio
 */
public class DefUseTestData {

	
	public void overwriteParam(int x, int y) {
		if (x == 0) y = 1;
		System.out.println(y);
	}
	
	public void localDataDependence() {
		int x;
		boolean b = true;
		if (b) {
			x = 1;
		} else {
			x = 2;
			System.err.println(x);  // x = 2
			x = 3;
		}
		System.err.println(x); // x = 1 or 3
		System.err.println(x); // x = 1 or 3
	}

	/**
	 * The method body has no meaning; this code is to use a finally block.
	 * @return
	 */
	public int tryFinallyDependence() {
		int x = 0;
		File f = new File("test");
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(f);
			x = stream.read();
			if (x == 0) {
				return x;
			} else {
				return x+1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) stream.close();
			} catch (IOException e) {
			}
		}
		return 0;
	}
	
	public int forStatement() {
		int x = 0;
		for (int i=0; i<100; i++) {
			x += i;
			System.out.println(i);
			if (i / 80 == 1) return x;
			System.out.println(x);
		}
		return x;
	}

	/**
	 * The same implementation as forStatement.
	 */
	public int whileStatement() {
		int x = 0;
		int i = 0;
		while (i<100) {
			x += i;
			System.out.println(i);
			if (i / 80 == 1) return x;
			System.out.println(x);
			i++;
		}
		return x;
	}
	
	public void withInnerClass() {
		
		final int k = 100;
		
		final Inner1 inner1 = new Inner1() {
			
			private int x = k;
			private int y = 100;
			
			public void printInner() {
				System.out.println(x);
				System.out.println(y);
			}
			
			@Override
			public void print() {
				printInner();
			}
		};
		
		Inner2 inner2 = new Inner2(inner1) {

			private int x = k;
			private Inner3 i = new Inner3(new Inner1());

			public void print() {
				System.out.println(x);
				System.out.println(i);
			}
		};
		inner2.print();
		
	}
	
	class Inner1 {
		
		public Inner1() {
		}
		
		public void print() {
		}
	}
	
	class Inner2 {
		private Inner1 arg;
		public Inner2(Inner1 arg) {
			this.arg = arg;
		}
		public void print() {
			arg.print();
		}

		class Inner3 {
			public Inner3(Inner1 arg) {
				
			}
		}
	}
}
