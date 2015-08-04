package soba.testdata;

public class ControlDependenceCode {

	/**
	 * This code fragment causes a loop of control dependency. 
	 */
	public static void main(String[] args) {
		int x = 0;
		for (String s : args) { // controls "if" statements in the loop.
			if (s == null) {
				continue;
			}
			if (s.length() == 1) { // Because this goes to the exit of this method, this statement controls the enclosing "for" statement. 
				test();
				return;
			} else if (s.length() == 2) {
				x = 2;
				continue;
			} else if (s.length() == 3) {
				x = 3;
			}
		}
		use(x);
	}
	
	private static void use(int x) {
		
	}
	
	private static void test() {
		
	}
}
