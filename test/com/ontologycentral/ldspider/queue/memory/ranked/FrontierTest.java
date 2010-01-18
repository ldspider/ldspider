package com.ontologycentral.ldspider.queue.memory.ranked;

import java.net.URI;
import java.util.Iterator;

import junit.framework.TestCase;

public class FrontierTest extends TestCase {
	public void testFrontier() throws Exception {
		Frontier f = new Frontier();
		
		URI u1 = new URI("http://harth.org/andreas/foaf.rdf");
		URI u2 = new URI("http://harth.org/andreas/foaf#ah");
		URI u3 = new URI("http://harth.org/andreas/foaf.rdf#ah");
		
		f.add(u1);
		f.add(u1);
		f.add(u2);
		f.add(u2);
		f.add(u3);

		Iterator<URI> it = f.getRanked();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}
