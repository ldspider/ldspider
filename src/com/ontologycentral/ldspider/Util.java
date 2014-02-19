package com.ontologycentral.ldspider;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class.
 * 
 * @author Tobias Kaefer
 */
public class Util {
	static Logger _log = Logger.getLogger(Util.class.getName());

	public static BufferedOutputStream createBufferedOutputStreamForHopwiseOperation(
			String filename, String extension, int hop) throws IOException {
		BufferedOutputStream ret;

		String outfilename = createFileNameForHopwiseOperation(filename,
				extension, hop);

		_log.info("Creating stream to " + outfilename);

		if (extension != null && extension.equals("gz")) {
			ret = new BufferedOutputStream(new GZIPOutputStream(
					new FileOutputStream(outfilename)));
		} else
			ret = new BufferedOutputStream(new FileOutputStream(outfilename));

		CrawlerConstants.CLOSER.add(ret);

		return ret;
	}

	public static String createFileNameForHopwiseOperation(String filename,
			String extension, int hop) {
		return filename
				+ "-"
				+ hop
				+ ((extension != null && extension.equals("")) ? "" : "."
						+ extension);
	}

	public static String[] determineFnameAndExtension(String filename) {
		int pathIdx = filename
				.lastIndexOf(System.getProperty("file.separator"));
		int extIdx = filename.lastIndexOf('.');

		String[] ret = new String[2];

		if (pathIdx > extIdx || extIdx < 0) {
			ret[0] = filename;
			ret[1] = "";
		} else {
			ret[0] = filename.substring(0, extIdx);
			ret[1] = filename.substring(extIdx + 1);
		}

		return ret;
	}

}
