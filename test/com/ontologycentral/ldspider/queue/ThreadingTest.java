package com.ontologycentral.ldspider.queue;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.tld.TldManager;


public class ThreadingTest extends TestCase {
	public static int THREADS = 64;
	
	public void testThreading() throws Exception {
		TldManager tldm = new TldManager();
		
		FetchQueue fq = new FetchQueue(tldm);

		Thread[] ts = new Thread[THREADS];
		
		for (int i = 0; i < THREADS; i++) {
			ts[i] = new Thread(new Worker(fq));
			ts[i].start();
		}
		
		for (int i = 0; i < THREADS; i++) {
			ts[i].join();
		}
	}	
}

class Worker implements Runnable {
	FetchQueue _fq;
	
	public Worker(FetchQueue fq) {
		_fq = fq;
	}

	public void run() {
		try {
			InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			int i = 0;

			String line = br.readLine();
			while (line != null) {
				i++;

				URI u = new URI(line);

				_fq.addFrontier(u);
				
				_fq.getSeen(u);

				line = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}