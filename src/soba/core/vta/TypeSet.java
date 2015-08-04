package soba.core.vta;

import java.util.List;

public class TypeSet {
	
	public static final String DEFAULT_UNKNOWN_TYPE = "java/lang/Object";
	public static final String DEFAULT_UNKNOWN_ARRAYTYPE = DEFAULT_UNKNOWN_TYPE + "[]";

	private TypeSetManager manager;
	private int typesId;
	private int approximatedTypesId;
	
	/**
	 * Creates a new <code>TypeSet</code> instance without type information.
	 * @param manager
	 */
	public TypeSet(TypeSetManager manager) {
		this.manager = manager;
		this.typesId = manager.getEmptyId();
		this.approximatedTypesId = manager.getEmptyId();
	}
	
	/**
	 * Creates a new <code>TypeSet</code> with a single type. 
	 * @param manager
	 * @param typename
	 */
	public TypeSet(TypeSetManager manager, String typename) { 
		this.manager = manager;
		assert typename != null;
		this.typesId = manager.getId(typename);
		this.approximatedTypesId = manager.getEmptyId();
	}

	/**
	 * Creates a new <code>TypeSet</code> with multiple types.
	 * @param manager
	 * @param parents
	 */
	public TypeSet(TypeSetManager manager, List<TypeSet> parents) {
		this.manager = manager;
		if (parents.size() == 0) {
			this.typesId = manager.getEmptyId();
			this.approximatedTypesId = manager.getEmptyId();
		} else if (parents.size() == 1) {
			this.typesId = parents.get(0).typesId;
			this.approximatedTypesId = parents.get(0).approximatedTypesId;
		} else {
			int mergeTypesId = parents.get(0).typesId;
			int mergeAppoximatedTypesId = parents.get(0).approximatedTypesId;
			for (int i=1; i<parents.size(); ++i) {
				mergeTypesId = manager.merge(mergeTypesId, parents.get(i).typesId);
				mergeAppoximatedTypesId = manager.merge(mergeAppoximatedTypesId, parents.get(i).approximatedTypesId);
			}
			this.typesId = mergeTypesId;
			this.approximatedTypesId = mergeAppoximatedTypesId;
		}
	}
	
	public TypeSet addType(String additionalType) {
		TypeSet copy = new TypeSet(manager);
		copy.typesId = manager.merge(this.typesId, manager.getId(additionalType));
		copy.approximatedTypesId = this.approximatedTypesId;
		return copy;
	}

	public static TypeSet createApproximation(TypeSetManager manager, String typename) {
		TypeSet t = new TypeSet(manager);
		t.approximatedTypesId = manager.getId(typename); 
		return t;
	}

	public TypeSet addApproximatedType(String additionalType) {
		TypeSet copy = new TypeSet(manager);
		copy.typesId = this.typesId;
		copy.approximatedTypesId = manager.merge(this.approximatedTypesId, manager.getId(additionalType));
		return copy;
	}
	

	public boolean contains(String t) {
		if (t == null) return false;
		String[] types = manager.getStrings(this.typesId);
		for (int i=0; i<types.length; ++i) {
			if (t.equals(types[i])) return true;
		}
		return false;
	}
	
	public int getTypeCount() { 
		return manager.getStrings(typesId).length;
	}
	
	public String getType(int index) {
		return manager.getStrings(typesId)[index];
	}
	
	public int getApproximatedTypeCount() {
		return manager.getStrings(approximatedTypesId).length;
	}
	
	public String getApproximatedType(int index) {
		return manager.getStrings(approximatedTypesId)[index];
	}
	
}
