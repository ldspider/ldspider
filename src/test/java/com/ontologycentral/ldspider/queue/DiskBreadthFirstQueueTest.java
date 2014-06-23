package com.ontologycentral.ldspider.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.util.NxUtil;
import org.semanticweb.yars.util.CallbackNxAppender;

@RunWith(Parameterized.class)
public class DiskBreadthFirstQueueTest {

	@Parameters(name = "sorting {1} URIs with counts {0} times")
	public static Collection<Object[]> data() {
		// number of times, number of URIs
		Object[][] parameters = new Object[][] { { 50, 0 }, { 50, 1 },
				{ 50, 2 }, { 50, 3 }, { 50, 4 }, { 50, 100 }, { 5, 9999 },
				{ 5, 10000 }, { 1, 100000 } };
		return Arrays.asList(parameters);
	}

	int _times, _uris;

	public DiskBreadthFirstQueueTest(int times, int uris) {
		_times = times;
		_uris = uris;
	}

	@Test
	public void testSortShort() throws IOException {
		for (int i = 1; i < _times; ++i)
			sort(_uris);
	}

	public void sort(int uricount) throws IOException {
		File tempfile = File.createTempFile(this.getClass().getSimpleName(),
				"nx");
		tempfile.deleteOnExit();

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(tempfile)));
		CallbackNxAppender cb = new CallbackNxAppender(bw);
		Random rand = new Random();

		Set<Node[]> ts = new TreeSet<Node[]>(DiskBreadthFirstQueue._nc);

		for (int i = 0; i < uricount; ++i) {
			Node[] nx = new Node[] {
					new Resource(NxUtil.escapeForNx("http://example.org/"
							+ rand.nextInt(999))),
					new Literal(Integer.toString(rand.nextInt(20))) };
			cb.processStatement(nx);
			ts.add(nx);
		}

		bw.close();

		File sortedFile = DiskBreadthFirstQueue.sort(this.getClass()
				.getSimpleName() + "_sorted", tempfile);

		FileReader fr = new FileReader(sortedFile);

		NxParser nxp = new NxParser(fr);

		try {
			for (Node[] nx : ts) {
				// elements are the same
				// assertTrue(DiskBreadthFirstQueue._nc.equals(nx, nxp.next()));
				assertEquals(new Nodes(nx), new Nodes(nxp.next()));
			}
		} catch (NoSuchElementException e) {
			fr.close();
			fail("gold standard is longer");
		}
		if (nxp.hasNext()) {
			fr.close();
			fail("gold standard is shorter");
		}

		fr.close();
	}
}
