package net.sourceforge.sitemaps;

import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

public class SitemapTest extends TestCase {
	public void testSitemap() throws Exception {
		URL u = new URL("http://www.bbc.co.uk/sitemap.xml");
	//"http://www.google.com/sitemap.xml");

		Sitemap s = new Sitemap(u);
		
		SitemapParser sp = new SitemapParser();
		sp.VERBOSE = true;
		//sp.DEBUG = true;
		
		URLConnection con = u.openConnection();
		
		Sitemap.SitemapType st = sp.processSitemap(s, "text/xml", con.getInputStream());
		
		System.out.println(st);
		System.out.println(s);
	}
}
