package com.ontologycentral.ldspider.frontier;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

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
	
	public void testDiskFrontier() throws Exception {
		DiskFrontier frontier = new DiskFrontier(new File("/tmp/frontier.txt"));
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));
		frontier.add(new URI("http://umbrich.net/foaf.rdf"));
		
		frontier.close();
		
		Iterator<URI> it = frontier.iterator();
		
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}
