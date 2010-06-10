package com.ontologycentral.ldspider.queue;

import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;
import com.ontologycentral.ldspider.tld.TldManager;

public class BreadthFirstQueueTest extends TestCase {
	public void testFrontier() throws Exception {
		Frontier f = new RankedFrontier();
		
		URI u1 = new URI("http://harth.org/andreas/foaf.rdf");
		URI u2 = new URI("http://harth.org/andreas/foaf#ah");
		URI u3 = new URI("http://harth.org/andreas/seealso.rdf");
		URI u4 = new URI("http://example.org/bla.rdf");
		
		f.add(u1);
		f.add(u1);
		f.add(u2);
		f.add(u2);
		f.add(u3);
		f.add(u4);

		TldManager tldm = new TldManager();

		BreadthFirstQueue bfsq = new BreadthFirstQueue(tldm, 2, 2);
		
		bfsq.schedule(f);
		
		System.out.println(bfsq);
		
		URI u = null;
		while ((u = bfsq.poll()) != null) {
			System.out.println("seen " + u);
		}
		
		bfsq.schedule(f);

		System.out.println(bfsq);
		
		while ((u = bfsq.poll()) != null) {
			System.out.println("seen " + u);
		}
		
		bfsq.schedule(f);

		System.out.println(bfsq);
	}
}
