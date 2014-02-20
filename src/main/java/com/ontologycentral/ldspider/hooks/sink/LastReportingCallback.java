package com.ontologycentral.ldspider.hooks.sink;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

/**
 * Callback that returns the context (in the benign case) of the last statement
 * processed.
 * 
 * @author Tobias
 * 
 */
public class LastReportingCallback implements Callback, LastReporter {

	Node _last = null;

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void processStatement(Node[] nx) {
		_last = nx[nx.length - 1];
	}

	public Resource whoWasLast() {
		if (_last instanceof Resource)
			return (Resource) _last;
		else
			return null;
	}
}
