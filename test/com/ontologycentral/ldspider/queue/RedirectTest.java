package com.ontologycentral.ldspider.queue;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.tld.TldManager;


public class RedirectTest extends TestCase {
	public void testNormalise() throws Exception {
		TldManager tldm = new TldManager();
		
		FetchQueue fq = new FetchQueue(tldm);
		
		URI u = new URI("http://dbpedia.org/resource/Karlsruhe");

		fq.addFrontier(u);
		
		fq.schedule(1);
		
		System.out.println(fq);
		
		System.out.println(fq.poll());
		
		System.out.println(fq);
		
		URI page = new URI("http://dbpedia.org/page/Karlsruhe");
		
		fq.setRedirect(u, page);
		
		System.out.println(fq);
		
		u = fq.poll();
		
		URI lu = fq.getRedirects().getRedir(u);

		System.out.println(lu);
	}
}