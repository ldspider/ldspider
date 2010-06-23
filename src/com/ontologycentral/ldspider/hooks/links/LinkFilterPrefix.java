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
}
