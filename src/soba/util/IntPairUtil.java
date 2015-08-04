package soba.util;

/**
 * A utility for collections of integer pairs.
 */
public class IntPairUtil {
	
	/**
	 * Creates an IntPairList from an IntPairSet.
	 * The resultant list is not sorted;
	 * the order of elements depends on the set's implementation.
	 */
	public static IntPairList createList(IntPairSet set) {
		final IntPairList list = new IntPairList(set.size());
		set.foreach(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				list.add(elem1, elem2);
				return true;
			}
		});
		return list;
	}
	
	
	/**
	 * Creates an IntPairList that includes two IntPairList sets.
	 */
	public static IntPairList createList(final IntPairSet set1, final IntPairSet set2) {
		final IntPairList list = new IntPairList(set1.size() + set2.size());
		set1.foreach(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				list.add(elem1, elem2);
				return true;
			}
		});
		set2.foreach(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				if (!set1.contains(elem1, elem2)) {
					list.add(elem1, elem2);
				}
				return true;
			}
		});
		return list;
		
	}

}
