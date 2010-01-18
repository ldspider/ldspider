package com.ontologycentral.ldspider.queue.memory.ranked;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Frontier {
	Map<URI, Integer> _data;
	
	public Frontier() {
		_data = Collections.synchronizedMap(new HashMap<URI, Integer>());
	}
	
	public void add(URI u) {
		Integer count = _data.get(u);
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		_data.put(u, count);
	}
	
	public void remove(URI u) {
		_data.remove(u);
	}

	public Iterator<URI> getRanked() {
		List<URI> li = new ArrayList<URI>();
		
		li.addAll(_data.keySet());
		
		Collections.sort(li, new CountComparator(_data));
		
		return li.iterator();
	}
}

class CountComparator implements Comparator<URI> {
	Map<URI, Integer> _map;
	
	public CountComparator(Map<URI, Integer> map) {
		_map = map;
	}
	
	public int compare(URI arg0, URI arg1) {
		return _map.get(arg1) - _map.get(arg0);
	}
}