package com.ontologycentral.ldspider;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class Callbacks extends Callback {
	Callback[] _cbs;
	
	public Callbacks(Callback... cbs) {
		_cbs = cbs;
	}

	@Override
	protected void startDocumentInternal() {
		for (Callback cb : _cbs) {
			cb.startDocument();
		}
	}

	@Override
	protected void endDocumentInternal() {
		for (Callback cb : _cbs) {
			cb.endDocument();
		}
	}

	@Override
	protected void processStatementInternal(Node[] nx) {
		for (Callback cb : _cbs) {
			cb.processStatement(nx);
		}		
	}
}