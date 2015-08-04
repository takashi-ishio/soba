package soba.core.vta;


import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.HashSet;

import soba.util.ObjectIdMap;

public class TypeSetManager {

	private ObjectIdMap<String> cache;
	private TIntObjectHashMap<String[]> strings;
	private static final String[] EMPTY = new String[0];
	private static final String SEPARATOR = "|";
	private static final String SEPARATOR_REGEX = "\\|";
	
	private TIntObjectHashMap<TIntIntHashMap> mergeMap;
	
	public TypeSetManager() {
		cache = new ObjectIdMap<String>();
		strings = new TIntObjectHashMap<String[]>();
		cache.add("");
		
		mergeMap = new TIntObjectHashMap<TIntIntHashMap>();
	}
	
	public int getEmptyId() {
		return cache.getId("");
	}
	
	public int getId(String singleString) {
		return cache.getId(singleString);
	}
	
	public int getId(String[] strings) {
		return cache.getId(toSingleString(strings));
	}
	
	public String[] getStrings(int id) {
		if (strings.containsKey(id)) {
			return strings.get(id);
		} else {
			String s = cache.getItem(id);
			if (s.length() == 0) {
				return EMPTY;
			} else {
				String[] array = s.split(SEPARATOR_REGEX);
				strings.put(id, array);
				return array;
			}
		}
	}
	
	public int merge(int id1, int id2) {
		if (id1 > id2) {
			int swap = id1;
			id1 = id2;
			id2 = swap;
		}
		TIntIntHashMap map = mergeMap.get(id1);
		if (map != null) {
			if (map.containsKey(id2)) {
				return map.get(id2);
			}
		}
		
		HashSet<String> union = new HashSet<String>();
		for (String s: getStrings(id1)) {
			union.add(s);
		}
		for (String s: getStrings(id2)) {
			union.add(s);
		}
		String[] unionStrings = union.toArray(EMPTY);
		Arrays.sort(unionStrings);
		int result = getId(unionStrings);
		if (map == null) {
			map = new TIntIntHashMap();
			mergeMap.put(id1, map);
		}
		map.put(id2, result);
		return result;
	}
	
	private String toSingleString(String[] types) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<types.length; ++i) {
			if (i > 0) b.append(SEPARATOR);
			b.append(types[i]);
		}
		return b.toString();
	}

	public int size() {
		int total = 0;
		for (int i=0; i<cache.size(); ++i) {
			total += cache.getItem(i).length();
		}
		return total;
	}
}
