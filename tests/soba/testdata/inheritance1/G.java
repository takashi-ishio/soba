package soba.testdata.inheritance1;

public class G extends C {

	public G() {
		super(0);
	}
	
	@Override
	void n() {
		super.n();
		System.err.println("G.n()");
	}
	
	public final void finalMethod() {
		
	}
	
	protected volatile int volatileField = 0;
	public transient int transientField = 0;
	private final int finalField = 0;
	
	
	public class InternalG {
		
		public InternalG() {
			super();
			System.out.println();
		}
		
		public void m() {
			finalMethod();
			n();
			System.out.println(finalField);
		}
		
	}
	
	public void x(int t) {
		System.out.println("G.x(int)");
	}

}
