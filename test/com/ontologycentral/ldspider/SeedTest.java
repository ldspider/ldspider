package com.ontologycentral.ldspider;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;

public class SeedTest extends TestCase {
	public void testSeed() throws Exception {
		Crawler c = new Crawler(2);
		
		List<URI> seeds = new ArrayList<URI>();
		seeds.add(new URI("http://www.w3.org/People/Berners-Lee/card"));

		ErrorHandler eh = new ErrorHandlerLogger(System.out, null);
		c.setErrorHandler(eh);
		c.setFetchFilter(new FetchFilterRdfXml(eh));

		c.evaluate(seeds, 0);
		
		eh.close();
	}	
}
