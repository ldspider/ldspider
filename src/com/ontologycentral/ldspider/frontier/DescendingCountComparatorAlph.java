package com.ontologycentral.ldspider.frontier;

import java.net.URI;
import java.util.Comparator;
import java.util.Map;

class DescendingCountComparatorAlph implements Comparator<URI> {
	Map<URI, Integer> _map;
	
	public DescendingCountComparatorAlph(Map<URI, Integer> map) {
		_map = map;
	}
	
	public int compare(URI arg0, URI arg1) {
		int result = _map.get(arg1) - _map.get(arg0);
		
		// if the two have the same count, order them alphabetically.
		if (result == 0)
			result = arg0.toASCIIString().compareTo(arg1.toASCIIString());
		return result;
	}
}