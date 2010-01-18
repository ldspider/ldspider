package com.ontologycentral.ldspider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;


public class CrawlerTest extends TestCase {
	public void testCrawl() throws Exception {
		Crawler c = new Crawler(1);
		
		List<URI> seeds = new ArrayList<URI>();
		seeds.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);

		c.evaluate(seeds, 0);
	}
	
	public void testCrawl2() throws Exception {
		Crawler c = new Crawler(1);

		List<URI> seeds = new ArrayList<URI>();
		seeds.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);

		c.evaluate(seeds, 0);

		seeds = new ArrayList<URI>();
		seeds.add(new URI("http://umbrich.net/foaf.rdf"));
		c.evaluate(seeds, 0);

		seeds = new ArrayList<URI>();
		seeds.add(new URI("http://umbrich.net/foaf.rdf"));
		c.evaluate(seeds, 0);		
	}
}

