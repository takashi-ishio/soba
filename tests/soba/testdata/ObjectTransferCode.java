package soba.testdata;

public class ObjectTransferCode {

	public int[][] newObject(int x) {
		ObjectTransferCode obj1 = new ObjectTransferCode();
		ObjectTransferCode obj2, obj3;
		obj2 = obj1;
		obj3 = null;
		m1(obj2);
		obj1 = m2(obj3);
		ObjectTransferCode[] array = new ObjectTransferCode[1];
		int[][] multiArray = new int[1][1];
		System.out.println(array.length);
//		System.out.println(multiArray.length);
		return multiArray;
	}
	
	public void m1(ObjectTransferCode obj) {
		
	}
	
	public ObjectTransferCode m2(ObjectTransferCode obj) {
		return obj;
	}
}
