package com.ontologycentral.ldspider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDummy;


public class RedirectTest extends TestCase {
//	public void testRedirectLoop() throws Exception {
//		Crawler c = new Crawler(1);
//		
//		List<URI> seeds = new ArrayList<URI>();
//		//seeds.add(new URI("https://secure.domaintools.com/login/?r=http://whois.domaintools.com/fenixdirecto.com"));
//		seeds.add(new URI("http://whois.domaintools.com/fenixdirecto.com"));
//		ErrorHandler eh = new ErrorHandlerLogger(null, null);
//		c.setErrorHandler(eh);
//
//		c.evaluate(seeds, 0);
//	}
	
	public void testRedirect() throws Exception {
		Crawler c = new Crawler(1);
		
		Frontier f = new BasicFrontier();
		f.add(new URI("http://dbpedia.org/resource/Karlsruhe"));
		
		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
		c.setLinkFilter(new LinkFilterDummy());

		c.evaluate(f, 1);
	}
}

