package com.ontologycentral.ldquery;


import junit.framework.TestCase;

import org.semanticweb.yars.util.CallbackNQOutputStream;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;

public class QueryEngineTest extends TestCase {
	public void testQuery() {
		String query = "SELECT * WHERE { <http://harth.org/andreas/foaf#ah> ?p ?o . ?o ?p2 ?o2 . }" ;

		QueryEngine qe = new QueryEngine();

		ErrorHandler eh = new ErrorHandlerLogger();
		qe.setErrorHandler(eh);
		
		qe.setOutputCallback(new CallbackNQOutputStream(System.out));

		qe.evaluate(query);
	}
}
