package com.ontologycentral.ldspider.hooks.links;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Add only uris with matching host to queue
 * @@@ Should be pld-level not host-level.
 * 
 * @author aharth
 *
 */
public class LinkFilterDomain extends LinkFilterDefault {
	
	private final Set<String> _prefixes;
	
	public LinkFilterDomain(Frontier f) {
		super(f);
		_prefixes = new HashSet<String>();
	}

	public void addHost(String pld) {
		_prefixes.add(pld);
	}
	
	public void setErrorHandler(ErrorHandler eh) {
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}
	
	@Override
	protected void addABox(Node[] nx, int i) {
		//Only add if the uri has a known prefix
		boolean found = false;
		for(String host : _prefixes) {
			if(nx[i].toString().startsWith(host))
				found = true;
		}
		if (found) {
			addABox(nx, i);
		}
	}
}
