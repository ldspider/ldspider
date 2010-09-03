package net.sourceforge.sitemaps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.sourceforge.sitemaps.Sitemap;

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
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

		StringBuilder sb = new StringBuilder();
		String l;
		while ((l = br.readLine()) != null) {
			sb.append(l);
			sb.append("\n");
		}
		
		//System.out.println(sb.toString());
		
		Sitemap.SitemapType st = sp.processSitemap(s, "text/xml", sb.toString());
		
		System.out.println(st);
		System.out.println(s);
	}
}
