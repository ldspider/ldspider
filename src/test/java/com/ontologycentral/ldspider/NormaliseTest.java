package com.ontologycentral.ldspider;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.Frontier;


public class NormaliseTest extends TestCase {
	public void testNormalise() throws Exception {
		URI u = new URI("http://Harth.org/andreas");
		
		System.out.println(u.getPath());
		System.out.println(Frontier.normalise(u));
	}
	
	public void testNormaliseHost() throws Exception {
		URI u = new URI("http://harth.org");
		System.out.println(Frontier.normalise(u));
	}
	
	public void testNormalise2() throws Exception {
		URI u = new URI("http://www.mademan.com/chickipedia/Special:URIResolver/angela_merkel");
		System.out.println(Frontier.normalise(u));
	}
}