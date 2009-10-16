package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.util.Set;

import org.semanticweb.yars.nx.parser.Callback;


/**
 * 
 * Just a wrapper to identify classes implementing the Callback interface 
 * for link selection
 *
 */
public interface LinkFilter extends Callback {

    Set<URI> getLinks();

}
