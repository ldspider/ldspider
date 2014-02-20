package com.ontologycentral.ldspider;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

public class RelativeUriTest extends TestCase {
	public void testUri() throws URISyntaxException {
		URI u = new URI("http://example.org/foo/bar");
		
		System.out.println(u.resolve("../baz"));
		
		
	}
}
