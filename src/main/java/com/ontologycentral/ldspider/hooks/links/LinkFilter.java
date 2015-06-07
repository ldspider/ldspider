package com.ontologycentral.ldspider.hooks.links;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;


/**
 * 
 * Just a wrapper to identify classes implementing the Callback interface 
 * for link selection
 *
 */
public abstract class LinkFilter extends Callback {
	abstract public void setErrorHandler(ErrorHandler e);
	abstract public void setFollowABox(boolean follow);
	abstract public void setFollowTBox(boolean follow);
}
