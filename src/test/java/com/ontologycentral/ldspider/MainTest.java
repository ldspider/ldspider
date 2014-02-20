package com.ontologycentral.ldspider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.queue.HashTableRedirects;
import com.ontologycentral.ldspider.queue.Redirects;
import com.ontologycentral.ldspider.seen.HashSetSeen;
import com.ontologycentral.ldspider.seen.Seen;

public class MainTest extends TestCase {
	public void testReadFromThisFileIntoThisRedirects() throws IOException,
			URISyntaxException {
		String[] suffixes = { ".gz", ".txt" };

		for (String suffix : suffixes) {

			Redirects redirects = new HashTableRedirects();

			File tempfile = File.createTempFile(this.getName(), suffix);
			tempfile.deleteOnExit();

			Map<URI, URI> testDataRedirects = new HashMap<URI, URI>();

			testDataRedirects.put(new URI("http://example1.org"), new URI(
					"http://example1.org/redirected"));
			testDataRedirects.put(new URI("http://example2.org"), new URI(
					"http://example2.org/redirected"));

			BufferedWriter bw = createBWforFile(tempfile);

			for (Entry<URI, URI> e : testDataRedirects.entrySet()) {
				bw.write("<" + e.getKey() + "> <" + e.getValue() + "> .");
				bw.write(System.getProperty("line.separator"));
			}

			bw.close();

			Main.readFromThisFileIntoThisRedirects(tempfile.getAbsolutePath(),
					redirects);

			for (Entry<URI, URI> e : testDataRedirects.entrySet()) {
				assertEquals(e.getValue(), redirects.getRedirect(e.getKey()));
			}
		}

	}

	public void testReadFromThisFileIntoThisSeen()
			throws FileNotFoundException, IOException, URISyntaxException {
		String[] suffixes = { ".gz", ".txt" };

		for (String suffix : suffixes) {

			Seen seen = new HashSetSeen();

			File tempfile = File.createTempFile(this.getName(), suffix);
			tempfile.deleteOnExit();

			Set<URI> testDataSeen = new HashSet<URI>();

			testDataSeen.add(new URI("http://example1.org"));
			testDataSeen.add(new URI("http://example2.org"));

			BufferedWriter bw = createBWforFile(tempfile);

			for (URI u : testDataSeen) {
				bw.write(u.toString());
				bw.write(System.getProperty("line.separator"));
			}

			bw.close();

			Main.readFromThisFileIntoThisSeen(tempfile.getAbsolutePath(), seen);

			for (URI u : testDataSeen) {
				assertTrue(seen.hasBeenSeen(u));
			}
		}
	}

	@SuppressWarnings("resource")
	public BufferedWriter createBWforFile(File f) throws FileNotFoundException,
			IOException {
		return new BufferedWriter(new OutputStreamWriter(
				f.getAbsolutePath().endsWith(".gz") ? new GZIPOutputStream(
						new FileOutputStream(f)) : new FileOutputStream(f)));

	}
}
