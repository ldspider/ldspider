package com.ontologycentral.ldspider.http.robot;

import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.http.ConnectionManager;

public class SitemapTest extends TestCase {
	public void testSitemap() throws Exception {
		URI u = new URI("http://www.google.com/bla");
	//"http://www.google.com/sitemap.xml");

		ConnectionManager cm = new ConnectionManager(null, 0, null, null, 10);

		Sitemaps s = new Sitemaps(cm);

		System.out.println(s.getSitemapUris(u));
	}
}