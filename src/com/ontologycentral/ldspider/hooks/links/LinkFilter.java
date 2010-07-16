package com.ontologycentral.ldspider.hooks.links;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;


/**
 * 
 * Just a wrapper to identify classes implementing the Callback interface 
 * for link selection
 *
 */
public interface LinkFilter extends Callback {
	public void setErrorHandler(ErrorHandler e);
	public void setFollowABox(boolean follow);
	public void setFollowTBox(boolean follow);
}
