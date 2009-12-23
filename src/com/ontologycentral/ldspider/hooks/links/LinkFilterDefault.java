package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Follow all links (subject, predicate, object).
 * 
 * @author aharth
 *
 */
public class LinkFilterDefault implements LinkFilter {
	Set<URI> _links;
	ErrorHandler _eh;
	
	public LinkFilterDefault(ErrorHandler eh) {
		_links = Collections.synchronizedSet(new HashSet<URI>());
		_eh = eh;
	}
	
	public Set<URI> getLinks() {
		return _links;
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	public void processStatement(Node[] nx) {
		for (int i = 0; i < nx.length-1; i++) {
			if (nx[i] instanceof Resource) {
				try {
					URI u = new URI(nx[i].toString());
					_links.add(u);
				} catch (URISyntaxException e) {
					_eh.handleError(e);
				}
			}
		}
	}
}
