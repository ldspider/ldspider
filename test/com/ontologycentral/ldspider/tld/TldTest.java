package com.ontologycentral.ldspider.tld;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.tld.TldManager;


public class TldTest extends TestCase {
	public void testNormalise() throws Exception {
		TldManager tldm = new TldManager();
				
		URI u = new URI("http://www.mademan.com/chickipedia/Special:URIResolver/angela_merkel");
		
		System.out.println(tldm.getPLD(u));
	}
}