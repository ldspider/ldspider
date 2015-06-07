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
public class LastReportingCallback extends Callback implements LastReporter {

	Node _last = null;

	public Resource whoWasLast() {
		if (_last instanceof Resource)
			return (Resource) _last;
		else
			return null;
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
		_last = nx[nx.length - 1];		
	}
}
