package com.ontologycentral.ldspider.hooks.links;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.frontier.Frontier;


/**
 * 
 * Just a wrapper to identify classes implementing the Callback interface 
 * for link selection
 *
 */
public interface LinkFilter extends Callback {
	public void setFrontier(Frontier f);
}
