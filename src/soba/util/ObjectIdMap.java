package soba.util;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;

public class ObjectIdMap<T> {
	
	private TObjectIntHashMap<T> map;
	private ArrayList<T> idToObject;
	private boolean frozen;
	
	/**
	 * Creates a new <code>ObjectIdMap</code> instance with the default size.
	 */
	public ObjectIdMap() {
		this(1024 * 1024);
	}

	/**
	 * Creates a new <code>ObjectIdMap</code> instance with a specified size.
	 * @param capacity
	 */
	public ObjectIdMap(int capacity) {
		map = new TObjectIntHashMap<T>(capacity * 2);
		idToObject = new ArrayList<T>(capacity);
		frozen = false;
	}
	
	/**
	 * Disable assigning new IDs.
	 * For frozen maps, getId method with a new object 
	 * returns -1 (or throws AssertionError).
	 */
	public void freeze() {
		frozen = true;
	}
	
	/**
	 * Adds a new object.
	 * @param s
	 */
	public void add(T s) {
		getId(s);
	}
	
	/**
	 * @param item specifies an object.
	 * @return the ID integer corresponding to the object.
	 * If a new object is given, this method returns a new id.
	 */
	public int getId(T item) {
		if (map.containsKey(item)) {
			return map.get(item);
		} else {
			if (frozen) {
				// A new object is added to the frozen id map.
				throw new FrozenMapException();
			} else {
				int newId = idToObject.size();
				map.put(item, newId);
				idToObject.add(item);
				return newId;
			}
		}
	}
	
	/**
	 * @param id
	 * @return an object.
	 */
	public T getItem(int id) {
		if ((id < 0) || (id >= idToObject.size())) return null;
		else return idToObject.get(id);
	}
	
	/**
	 * @return the number of elements.
	 */
	public int size() {
		return idToObject.size();
	}
	
	public static class FrozenMapException extends RuntimeException {
		
		private static final long serialVersionUID = -1682458461699095201L;

		public FrozenMapException() {
			super();
		}
	}

}
