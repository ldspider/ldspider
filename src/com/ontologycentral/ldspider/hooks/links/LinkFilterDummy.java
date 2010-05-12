package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Follow no links
 * 
 * @author aharth
 *
 */
public class LinkFilterDummy implements LinkFilter {

	public Set<URI> getLinks() {
		return new HashSet<URI>();
	}
	
	public void setFollowABox(boolean follow) {
		;
	}

	public void setFollowTBox(boolean follow) {
		;
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

	public void setErrorHandler(ErrorHandler eh) {
		;
	}
}
