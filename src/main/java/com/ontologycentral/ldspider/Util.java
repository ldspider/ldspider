package com.ontologycentral.ldspider;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
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

	public static class LineByLineIterable implements Iterable<String> {

		final BufferedReader _br;

		public LineByLineIterable(BufferedReader br) {
			_br = br;
		}

		public Iterator<String> iterator() {
			return new Iterator<String>() {
				String next = null;
				boolean nextIsFresh = false;

				@Override
				public boolean hasNext() {
					if (nextIsFresh && next != null)
						return true;
					else {
						produceNext();
						if (nextIsFresh && next != null)
							return true;
						else
							return false;
					}
				}

				private void produceNext() {
					if (nextIsFresh && next == null)
						// Reader has ended
						;
					else {
						try {
							next = _br.readLine();
						} catch (IOException e) {
							next = null;
							e.printStackTrace();
						}
						nextIsFresh = true;
					}
				}

				@Override
				public String next() {
					if (hasNext()) {
						nextIsFresh = false;
						return next;
					} else
						throw new NoSuchElementException();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		};

	}

	public static class StringToURIiterable implements Iterable<URI> {
		final Iterable<String> _it;

		public StringToURIiterable(Iterable<String> it) {
			_it = it;
		}

		@Override
		public Iterator<URI> iterator() {
			return new Iterator<URI>() {
				Iterator<String> stringIt = _it.iterator();

				URI next = null;
				boolean nextIsFresh = false;

				@Override
				public boolean hasNext() {
					if (nextIsFresh && next != null)
						return true;
					else {
						produceNext();
						if (nextIsFresh && next != null)
							return true;
						else
							return false;
					}
				}

				public void produceNext() {
					if (nextIsFresh && next == null)
						// Reader has ended
						;
					else {
						if (stringIt.hasNext()) {
							String s = stringIt.next();
							try {
								next = new URL(s).toURI();
								nextIsFresh = true;
							} catch (URISyntaxException e) {
								produceNext();
								_log.fine("Discard invalid uri "
										+ e.getMessage() + " for " + s);
							} catch (MalformedURLException e) {
								produceNext();
								_log.fine("Discard invalid uri "
										+ e.getMessage() + " for " + s);
							}
						} else {
							nextIsFresh = true;
							next = null;
						}
					}
				}

				@Override
				public URI next() {
					if (hasNext()) {
						nextIsFresh = false;
						return next;
					} else
						throw new NoSuchElementException();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

	}
}
