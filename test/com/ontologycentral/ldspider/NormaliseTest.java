package com.ontologycentral.ldspider;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.queue.FetchQueue;


public class NormaliseTest extends TestCase {
	public void testNormalise() throws Exception {
		FetchQueue fq = new FetchQueue(null);
		
		URI u = new URI("http://Harth.org/andreas");
		
		System.out.println(u.getPath());
		System.out.println(fq.normalise(u));
	}
	
	public void testNormaliseHost() throws Exception {
		FetchQueue fq = new FetchQueue(null);
		
		URI u = new URI("http://harth.org");
		System.out.println(fq.normalise(u));
	}
	
	public void testNormalise2() throws Exception {
		FetchQueue fq = new FetchQueue(null);
		
		URI u = new URI("http://www.mademan.com/chickipedia/Special:URIResolver/angela_merkel");
		System.out.println(fq.normalise(u));
	}
}