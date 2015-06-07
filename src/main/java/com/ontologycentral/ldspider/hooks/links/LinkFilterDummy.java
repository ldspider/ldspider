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
public class LinkFilterDummy extends LinkFilter {

	public Set<URI> getLinks() {
		return new HashSet<URI>();
	}
	
	public void setFollowABox(boolean follow) {
		;
	}

	public void setFollowTBox(boolean follow) {
		;
	}

	public void setErrorHandler(ErrorHandler eh) {
		;
	}

	@Override
	protected void startDocumentInternal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endDocumentInternal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processStatementInternal(Node[] nx) {
		// TODO Auto-generated method stub
		
	}
}
