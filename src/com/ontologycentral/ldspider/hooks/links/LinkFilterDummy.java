package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

/**
 * Follow no links
 * 
 * @author aharth
 *
 */
public class LinkFilterDummy implements LinkFilter {

	public Set<URI> getLinks() {
		return null;
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	public void processStatement(Node[] arg0) {
		;
	}
}
