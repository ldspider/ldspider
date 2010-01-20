package com.ontologycentral.ldspider.queue.memory;

import java.net.URI;
import java.util.Iterator;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;

public class FrontierTest extends TestCase {
	public void testFrontier() throws Exception {
		Frontier f = new RankedFrontier(null);
		
		URI u1 = new URI("http://harth.org/andreas/foaf.rdf");
		URI u2 = new URI("http://harth.org/andreas/foaf#ah");
		URI u3 = new URI("http://harth.org/andreas/foaf.rdf#ah");
		
		f.add(u1);
		f.add(u1);
		f.add(u2);
		f.add(u2);
		f.add(u3);

		Iterator<URI> it = f.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}
