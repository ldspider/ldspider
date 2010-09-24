package com.ontologycentral.ldspider.tld;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.http.ConnectionManager;


public class TldTest extends TestCase {
	public void testNormalise() throws Exception {
		TldManager tldm = new TldManager();
				
		URI u = new URI("http://www.mademan.com/chickipedia/Special:URIResolver/angela_merkel");
		
		System.out.println(tldm.getPLD(u));
	}
	
	public void testQuery() throws Exception {
		TldManager tldm = new TldManager();

		URI u = new URI("http://www.rkbexplorer.com/sameAs/?uri=http%3A%2F%2Facm.rkbexplorer.com%2Fid%2Fperson-715486-950006ecd385595c1641ecb94ea283a1");
		
		System.out.println(u.getQuery());
		System.out.println(tldm.getPLD(u));
	}
	
	public void testPerformance() throws Exception {
		long time = System.currentTimeMillis();

		TldManager tldm = new TldManager();
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		
		String line = br.readLine();
		while (line != null) {
			i++;

			URI u = new URI(line);
			
			String pld = tldm.getPLD(u);
			
			if ((i % 10000) == 0) {
				System.out.println("pld for " + u + " is " + pld);
			}
			
			line = br.readLine();
		}
		
		br.close();
		
		long time1 = System.currentTimeMillis();
		
		System.out.println(i + " uris in " + (time1-time) + " ms");
	}
	
	public void testLookup() throws Exception {
	    ConnectionManager cm = new ConnectionManager(null, 0, null, null, 1);

		TldManager tldm = new TldManager(cm);
				
		URI u = new URI("http://www.mademan.com/chickipedia/Special:URIResolver/angela_merkel");
		
		System.out.println(tldm.getPLD(u));
	}
}