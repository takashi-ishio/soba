package soba.testdata;


import soba.testdata.inheritance1.D;

public class ReflectionCode {

	
	public void newInstanceUser() {
		Class<?> c = D.class;
		try {
			Object o = c.newInstance();
			System.out.print(o.toString());
			
			D obj = (D)o;
			System.out.print(obj.toString());
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		}
	}

	public void newInstanceUser2() {
		Class<?> c = D.class;
		try {
			Object o = c.newInstance();
			System.out.print(o.toString());
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		}
	}
	

}
