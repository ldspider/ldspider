package com.ontologycentral.ldspider.frontier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.util.NxUtil;
import org.semanticweb.yars.util.CallbackNxAppender;

public class SortingDiskFrontierTest {

	@Test
	public void testSort() throws IOException, ParseException {
		File tempfile = File.createTempFile(this.getClass().getSimpleName(),
				"nx");
		tempfile.deleteOnExit();

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(tempfile)));
		CallbackNxAppender cb = new CallbackNxAppender(bw);

		List<Resource> goldstandard = new LinkedList<Resource>();

		Resource r = null;

		for (int i = 3; i > 0; --i) {
			r = new Resource(NxUtil.escapeForNx("http://example.org/"
					+ "äöß\u06e9" + i));
			cb.processStatement(new Node[] { r });
			goldstandard.add(r);
		}

		cb.processStatement(new Node[] { r });
		goldstandard.add(r);

		bw.close();

		Collections.sort(goldstandard);

		SortingDiskFrontier sdf = new SortingDiskFrontier();
		File outfile = sdf.sort(tempfile);

		FileReader fr = new FileReader(outfile);

		NxParser nxp = new NxParser(fr);

		try {
			for (Node n : goldstandard) {
				assertEquals(new Nodes(n), new Nodes(nxp.next()));
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
		sdf.close();

	}

}
