package com.ontologycentral.ldspider.frontier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RankedFrontier extends Frontier {
	Map<URI, Integer> _data;
	
	public RankedFrontier() {
		super();
		_data = Collections.synchronizedMap(new HashMap<URI, Integer>());
	}
	
	public void add(URI u) {
		u = process(u);

		if (u != null) {
			Integer count = _data.get(u);
			if (count == null) {
				count = 1;
			} else {
				count++;
			}
			_data.put(u, count);

			_log.fine("added " + u);
		}
	}
	
	public void remove(URI u) {
		_data.remove(u);
	}

	public Iterator<URI> iterator() {
		List<URI> li = new ArrayList<URI>();
		
		li.addAll(_data.keySet());
		
		Collections.sort(li, new CountComparator(_data));
		
		return li.iterator();
	}

	public void removeAll(Collection<URI> c) {
		for (URI u: c) {
			remove(u);
		}
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