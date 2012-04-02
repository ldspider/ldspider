package com.ontologycentral.ldspider.frontier;

import java.util.Comparator;
import java.util.Map;

public class DescendingCountComparatorAlph<T> implements Comparator<T> {
	Map<String, Integer> _map;
	
	public DescendingCountComparatorAlph(Map<String, Integer> map) {
		_map = map;
	}
	
	public int compare(T arg0, T arg1) {
		int result = _map.get(arg1.toString()) - _map.get(arg0.toString());
		
		// if the two have the same count, order them alphabetically.
		if (result == 0)
			result = arg0.toString().compareTo(arg1.toString());
		return result;
	}
}