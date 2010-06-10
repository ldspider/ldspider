package com.ontologycentral.ldspider.queue;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;
import com.ontologycentral.ldspider.queue.BreadthFirstQueue;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.tld.TldManager;


public class ThreadingPollTest extends TestCase {
	public static int THREADS = 64;
	
	public void testThreading() throws Exception {
		TldManager tldm = new TldManager();
		
		SpiderQueue fq = new BreadthFirstQueue(tldm, 5);
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		Frontier f = new RankedFrontier();
		
		String line = br.readLine();
		while (line != null) {
			URI u = new URI(line);
			
			f.add(u);
			
			line = br.readLine();
		}
		
		br.close();
		
		fq.schedule(f);
		
		Thread[] ts = new Thread[THREADS];
		
		for (int i = 0; i < THREADS; i++) {
			ts[i] = new Thread(new PWorker(fq));
			ts[i].start();
		}
		
		for (int i = 0; i < THREADS; i++) {
			ts[i].join();
		}
	}		
}

class PWorker implements Runnable {
	SpiderQueue _fq;
	URI _uri;
	
	public PWorker(SpiderQueue fq) {
		_fq = fq;
	}

	public void run() {
		URI u = null;
		do {
			u = _fq.poll();
			if (u != null) {
				if (u.equals(_uri)) {
					throw new RuntimeException(u + " == " + _uri);
				}
				_uri = u;
				System.out.print(".");
			}
		} while (u != null);
	}
}