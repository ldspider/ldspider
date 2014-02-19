package com.ontologycentral.ldspider.seen;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HashSetSeen implements Seen {

	Set<URI> _set;

	public HashSetSeen() {
		_set = Collections.synchronizedSet(new HashSet<URI>());
	}

	public boolean hasBeenSeen(URI u) {
		return _set.contains(u);
	}

	public boolean add(Collection<URI> uris) {
		return _set.addAll(uris);
	}

	public boolean add(URI uri) {
		return _set.add(uri);
	}

	public void clear() {
		_set.clear();

	}

}
