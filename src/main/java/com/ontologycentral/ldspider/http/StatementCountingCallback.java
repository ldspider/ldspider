package com.ontologycentral.ldspider.http;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

/**
 * A {@link org.semanticweb.yars.nx.parser.Callback Callback} counting the
 * statements it has been supplied.
 * 
 * @author Tobias Käfer
 * 
 */

public class StatementCountingCallback extends Callback {

	int _stmtCount;

	public StatementCountingCallback() {
		_stmtCount = 0;
	}

	public StatementCountingCallback reset() {
		_stmtCount = 0;
		return this;
	}

	public int getStmtCount() {
		return _stmtCount;
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
		++_stmtCount;		
	}
}
