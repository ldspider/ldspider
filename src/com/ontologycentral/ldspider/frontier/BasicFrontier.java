package com.ontologycentral.ldspider.frontier;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BasicFrontier extends Frontier {
	Set<URI> _data;
	
	public BasicFrontier() {
		super();
		_data = Collections.synchronizedSet(new HashSet<URI>());
	}
	
	public void add(URI u) {
		u = process(u);
		if (u != null) {
			_data.add(u);
		}
	}
	
	public void remove(URI u) {
		_data.remove(u);
	}

	public Iterator<URI> iterator() {
		return _data.iterator();
	}

	public void addAll(Collection<URI> c) {
		_data.addAll(c);
	}

	public void removeAll(Collection<URI> c) {
		_data.removeAll(c);
	}
}