package com.ontologycentral.ldspider.hooks.links;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Add only uris with matching prefix to queue
 *
 * @author RobertIsele
 *
 */
public class LinkFilterPrefix extends LinkFilterDefault {
	
	private final Set<String> _prefixes;
	
	public LinkFilterPrefix(Frontier f) {
		super(f);
		_prefixes = new HashSet<String>();
	}

	public void addPrefix(String prefix) {
		_prefixes.add(prefix);
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
	protected void addUri(Node[] nx, int i) {
		//Only add if the uri has a known prefix
		boolean found = false;
		for(String prefix : _prefixes) {
			if(nx[i].toString().startsWith(prefix))
				found = true;
		}
		if (found) {
			super.addUri(nx, i);
		}
	}
}
