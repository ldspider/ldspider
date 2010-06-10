package com.ontologycentral.ldspider.queue;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.queue.BreadthFirstQueue;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.tld.TldManager;


public class RedirectTest extends TestCase {
	public void testNormalise() throws Exception {
		TldManager tldm = new TldManager();
		
		SpiderQueue fq = new BreadthFirstQueue(tldm, 1);
		
		URI u = new URI("http://dbpedia.org/resource/Karlsruhe");

		Frontier f = new BasicFrontier();
		
		f.add(u);
		
		fq.schedule(f);
		
		System.out.println(fq);
		
		System.out.println(fq.poll());
		
		System.out.println(fq);
		
		URI page = new URI("http://dbpedia.org/page/Karlsruhe");
		
		fq.setRedirect(u, page, 303);
		
		System.out.println(fq);
		
		u = fq.poll();
		
		//URI lu = fq.obtainRedirect(u);

		//System.out.println(lu);
		
		page = new URI("http://dbpedia.org/index.html");

		System.out.println(page.getPath());
		System.out.println(Frontier.normalise(page));
		
		page = new URI("http://dbpedia.org/bla/index.html");

		System.out.println(page.getPath());
		System.out.println(Frontier.normalise(page));
	}
}