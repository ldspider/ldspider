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

public class StatementCountingCallback implements Callback {

	int _stmtCount;

	public StatementCountingCallback() {
		_stmtCount = 0;
	}

	public void endDocument() {

	}

	public void processStatement(Node[] arg0) {
		++_stmtCount;
	}

	public void startDocument() {

	}

	public StatementCountingCallback reset() {
		_stmtCount = 0;
		return this;
	}

	public int getStmtCount() {
		return _stmtCount;
	}
}
