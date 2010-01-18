package com.ontologycentral.ldspider.queue.memory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.queue.memory.FetchQueue;
import com.ontologycentral.ldspider.tld.TldManager;


public class PerformanceTest extends TestCase {
	public void testNormalise() throws Exception {
		long time = System.currentTimeMillis();

		TldManager tldm = new TldManager();
		
		FetchQueue fq = new FetchQueue(tldm, Integer.MAX_VALUE);
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		
		String line = br.readLine();
		while (line != null) {
			i++;

			URI u = new URI(line);
			
			fq.addFrontier(u);
			
			line = br.readLine();
		}
		
		br.close();
		
		fq.schedule();
		
		int size = fq.size();
		
		URI u = fq.poll();
		Random r = new Random();

		int j = 0;
		int redirects = 0;
		
		while (u != null) {
			u = fq.poll();
			
			if (u != null && r.nextFloat() < 0.01) {
				fq.setRedirect(u, new URI("http://dbpedia.org/resource/Redirect"), 303);
				fq.addDirectly(u);
				System.out.println("adding " + u);
				redirects++;
			}
			j++;
		}
		
		long time1 = System.currentTimeMillis();
		
		System.out.println(fq);
		System.out.println(fq.size());
		System.out.println("initial queue size " + size);
		System.out.println("redirects " + redirects);
		
		System.out.println(i + " uris, " + j + " polled, in " + (time1-time) + " ms");
	}
}