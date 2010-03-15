package com.ontologycentral.ldspider.frontier;

import java.net.URI;

import junit.framework.TestCase;

public class FrontierTest extends TestCase {
	public void testFrontier() throws Exception {
		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));
		frontier.add(new URI("http://umbrich.net/foaf.rdf"));
		
		System.out.println(frontier);

//		ErrorHandler eh = new ErrorHandlerLogger(null, null);
//		c.setErrorHandler(eh);
//
//		c.evaluateBreadthFirst(frontier, 2, -1);
	}
}
