package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

public class UriSrc implements Callback {
	Map<URI, Set<URI>> _urisrc;
	
	public UriSrc() {
		_urisrc = new Hashtable<URI, Set<URI>>();
	}
	
	public Set<URI> getSources(URI uri) {
		return _urisrc.get(uri);
	}
	
	public void processStatement(Node[] nx) {		
		for (int i=0; i < nx.length-1; i++) {
			URI src;
			try {
				src = new URI(nx[nx.length-1].toString());
				if (nx[i] instanceof Resource) {
					URI u = new URI(nx[i].toString());
					Set<URI> li = _urisrc.get(u);
					if (li == null) {
						li = new HashSet<URI>();
						_urisrc.put(u, li);
					}
					li.add(src);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public void startDocument() {
		;
	}

	public void endDocument() {
		;
	}
}
