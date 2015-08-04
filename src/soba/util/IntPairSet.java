package soba.util;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;

/**
 * This class represents a set of integer pairs.
 */
public class IntPairSet {

	private TIntObjectHashMap<TIntHashSet> intPairs;
	private int pairCount;
	
	/**
	 * Creates a new <code>IntPairSet</code> instance.
	 */
	public IntPairSet() {
		intPairs = new TIntObjectHashMap<TIntHashSet>();
		pairCount = 0;
	}
	
	/**
	 * Adds a pair of integers.
	 * If the same pair of integers has been already stored,
	 * this method does not change the state.
	 * @param elem1 specifies a first value.
	 * @param elem2 specifies a second value.
	 */
	public void add(int elem1, int elem2) {
		TIntHashSet secondValues = intPairs.get(elem1);
		if (secondValues == null) {
			secondValues = new TIntHashSet();
			intPairs.put(elem1, secondValues);
		}
		boolean added = secondValues.add(elem2);
		if (added) {
			++pairCount;
		}
	}
	
	/**
	 * @return true if the set contains a pair (elem1, elem2).
	 */
	public boolean contains(int elem1, int elem2) {
		TIntHashSet secondValues = intPairs.get(elem1);
		if (secondValues != null) {
			return secondValues.contains(elem2);
		} else {
			return false;
		}
	}
	
	/**
	 * @return true if the set contains a pair (elem1, _).
	 */
	public boolean containsFirst(int elem1) {
		TIntHashSet secondValues = intPairs.get(elem1);
		if (secondValues != null) {
			return !secondValues.isEmpty();
		} else {
			return false;
		}
	}
	
	/**
	 * @return the number of pairs in the set.
	 */
	public int size() {
		return pairCount;
	}
	
	/**
	 * Executes a procedure for each element.
	 * @param proc
	 */
	public void foreach(final IntPairProc proc) { 
		intPairs.forEachEntry(new IntPairIterator(proc));
	}
	
	private class IntPairIterator implements TIntObjectProcedure<TIntHashSet> {
		
		private IntPairProc proc;
		private boolean continueFlag;
		
		public IntPairIterator(IntPairProc proc) {
			this.proc = proc;
			this.continueFlag = true;
		}
		
		@Override
		public boolean execute(final int firstValue, TIntHashSet secondValues) {
			secondValues.forEach(new TIntProcedure() {
				
				@Override
				public boolean execute(int secondValue) {
					continueFlag = proc.execute(firstValue, secondValue);
					return continueFlag;
				}
			});			
			return continueFlag;
		}
	}
}
