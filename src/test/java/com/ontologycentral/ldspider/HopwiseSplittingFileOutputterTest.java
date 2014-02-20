package com.ontologycentral.ldspider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

public class HopwiseSplittingFileOutputterTest extends TestCase {

	int hops = 3;

	public void testNoHops() throws IOException {
		String[] filenames = { Integer.toString(this.hashCode()),
				new String(this.hashCode() + ".gz") };

		String testString = "lksdfjlskdflksdjf\nlsdfsdjfdslkjfdsfj\n";

		for (String fileName : filenames) {
			HopwiseSplittingFileOutputter put = new HopwiseSplittingFileOutputter(
					fileName);
			put.append(testString);

			put.close();

			StringBuilder sb = new StringBuilder();

			InputStream is;

			String[] splittedFileName = Util
					.determineFnameAndExtension(fileName);

			if (fileName.endsWith(".gz"))
				is = new GZIPInputStream(new FileInputStream(
						Util.createFileNameForHopwiseOperation(
								splittedFileName[0], splittedFileName[1], 0)));
			else
				is = new BufferedInputStream(new FileInputStream(
						Util.createFileNameForHopwiseOperation(
								splittedFileName[0], splittedFileName[1], 0)));

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			while ((line = br.readLine()) != null)
				sb.append(line + "\n");

			assertEquals(testString, sb.toString());

			br.close();

			new File(Util.createFileNameForHopwiseOperation(
					splittedFileName[0], splittedFileName[1], 0))
					.deleteOnExit();
		}
	}

	public void testHopsRetaining() throws IOException {
		String[] filenames = { Integer.toString(this.hashCode()),
				new String(this.hashCode() + ".gz") };

		String testString = "lksdfjlskdflksdjf\nlsdfsdjfdslkjfdsfj\n";
		for (String fileName : filenames) {

			String[] splittedFileName = Util
					.determineFnameAndExtension(fileName);

			HopwiseSplittingFileOutputter put = new HopwiseSplittingFileOutputter(
					fileName, true);
			for (int i = 0; i < hops; ++i) {

				put.append(testString);

				put.nextHop();

			}

			put.close();

			for (int i = 0; i < hops; ++i) {

				StringBuilder sb = new StringBuilder();

				InputStream is;

				if (fileName.endsWith(".gz"))
					is = new GZIPInputStream(
							new FileInputStream(
									Util.createFileNameForHopwiseOperation(
											splittedFileName[0],
											splittedFileName[1], i)));
				else
					is = new BufferedInputStream(
							new FileInputStream(
									Util.createFileNameForHopwiseOperation(
											splittedFileName[0],
											splittedFileName[1], i)));

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				String line = null;

				while ((line = br.readLine()) != null)
					sb.append(line + "\n");

				StringBuilder tsb = new StringBuilder();

				for (int j = 0; j <= i; ++j) {
					tsb.append(testString);
				}

				assertEquals(tsb.toString(), sb.toString());

				br.close();

				new File(Util.createFileNameForHopwiseOperation(
						splittedFileName[0], splittedFileName[1], i))
						.deleteOnExit();

			}
			new File(Util.createFileNameForHopwiseOperation(
					splittedFileName[0], splittedFileName[1], hops))
					.deleteOnExit();
		}
	}

	public void testHopsNotRetaining() throws IOException {
		String[] filenames = { Integer.toString(this.hashCode()),
				new String(this.hashCode() + ".gz") };

		String testString = "lksdfjlskdflksdjf\nlsdfsdjfdslkjfdsfj\n";
		for (String fileName : filenames) {
			String[] splittedFileName = Util
					.determineFnameAndExtension(fileName);

			HopwiseSplittingFileOutputter put = new HopwiseSplittingFileOutputter(
					fileName, false);
			for (int i = 0; i < hops; ++i) {

				put.append(testString);

				put.nextHop();

			}

			put.close();

			for (int i = 0; i < hops; ++i) {

				StringBuilder sb = new StringBuilder();

				InputStream is;

				if (fileName.endsWith(".gz"))
					is = new GZIPInputStream(
							new FileInputStream(
									Util.createFileNameForHopwiseOperation(
											splittedFileName[0],
											splittedFileName[1], i)));
				else
					is = new BufferedInputStream(
							new FileInputStream(
									Util.createFileNameForHopwiseOperation(
											splittedFileName[0],
											splittedFileName[1], i)));

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				String line = null;

				while ((line = br.readLine()) != null)
					sb.append(line + "\n");

				assertEquals(testString, sb.toString());

				br.close();

				new File(Util.createFileNameForHopwiseOperation(
						splittedFileName[0], splittedFileName[1], i))
						.deleteOnExit();

			}

			new File(Util.createFileNameForHopwiseOperation(
					splittedFileName[0], splittedFileName[1], hops))
					.deleteOnExit();
		}
	}
}
