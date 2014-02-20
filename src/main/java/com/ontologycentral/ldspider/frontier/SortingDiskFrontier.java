package com.ontologycentral.ldspider.frontier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.NodeComparator.NodeComparatorArgs;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.sort.SortIterator;
import org.semanticweb.yars.nx.sort.SortIterator.SortArgs;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;
import org.semanticweb.yars.util.Node2uriConvertingIterator;
import org.semanticweb.yars.util.PleaseCloseTheDoorWhenYouLeaveIterator;

import com.ontologycentral.ldspider.CrawlerConstants;

/**
 * 
 * @author Tobias Kaefer
 * 
 */
public class SortingDiskFrontier extends Frontier {

	Logger _log = Logger.getLogger(this.getClass().getName());

	BufferedWriter _bw;

	static final String FILENAME_BASE = "ldspider-diskFrontierTmp";
	static final String FILENAME_CURRENT = FILENAME_BASE + "-Current";
	static final String FILENAME_SORTED = FILENAME_BASE + "-Sorted";

	File _currentTempFile = null;
	File _sortedTempFile = null;
	boolean _isSorted;

	final boolean _sortBeforeIterating;
	final boolean _gzipFrontier;
	
	final String SUFFIX;

	public SortingDiskFrontier() throws IOException {
		this(CrawlerConstants.DISKFRONTIER_SORT_BEFORE_ITERATING,
				CrawlerConstants.DISKFRONTIER_GZIP_FRONTIER);
	}

	public SortingDiskFrontier(boolean sort, boolean gzip) throws IOException {
		_isSorted = false;
		_sortBeforeIterating = sort;
		_gzipFrontier = gzip;
		SUFFIX = _gzipFrontier ? ".nx.gz" : ".nx";
		_currentTempFile = File.createTempFile(FILENAME_CURRENT, SUFFIX);
		_currentTempFile.deleteOnExit();
		OutputStream os = _gzipFrontier ? new GZIPOutputStream(
				new FileOutputStream(_currentTempFile)) : new FileOutputStream(
				_currentTempFile);
		_bw = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void add(URI u) {
		u = process(u);
		if (u == null)
			return;
		try {
			_bw.write("<");
			_bw.write(u.toString());
			_bw.write("> .\n");
			_isSorted = false;
		} catch (IOException e) {
			// _log.warning(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void removeAll(Collection<URI> c) {
		throw new UnsupportedOperationException("Can't remove items from a "
				+ SortingDiskFrontier.class.getSimpleName());
	}

	@Override
	public void reset() {
		try {
			_bw.close();
		} catch (IOException e1) {
			_log.warning(e1.getMessage());
		}
		_currentTempFile.delete();
		if (_sortedTempFile != null)
			_sortedTempFile.delete();
		try {
			_currentTempFile = File.createTempFile(FILENAME_CURRENT, SUFFIX);
		} catch (IOException e) {
			_log.warning(e.getMessage());
		}
		_currentTempFile.deleteOnExit();
		try {
			OutputStream os = _gzipFrontier ? new GZIPOutputStream(
					new FileOutputStream(_currentTempFile))
					: new FileOutputStream(_currentTempFile);
			_bw = new BufferedWriter(new OutputStreamWriter(os));
		} catch (IOException e) {
			_log.warning(e.getMessage());
		}
		_isSorted = false;
	}

	@Override
	public Iterator<URI> iterator() {

		final NxParser nx;
		final BufferedReader br;

		try {
			_bw.close();

			if (!_isSorted && _sortBeforeIterating) {
				_sortedTempFile = sort(_currentTempFile);
				_sortedTempFile.deleteOnExit();
			}

			File file = _sortBeforeIterating ? _sortedTempFile : _currentTempFile;
			
			InputStream is = _gzipFrontier ? new GZIPInputStream(
					new FileInputStream(file)) : new FileInputStream(
					file);

			br = new BufferedReader(new InputStreamReader(is));

			nx = new NxParser(br);
		} catch (IOException e) {
			_log.warning("IOException. " + e.getLocalizedMessage()
					+ ". returning empty iterator!");
			return Collections.<URI>emptyList().iterator();
		} catch (ParseException e) {
			_log.warning("ParseException. " + e.getLocalizedMessage()
					+ ". returning empty iterator!");
			return Collections.<URI>emptyList().iterator();
		}

		return new PleaseCloseTheDoorWhenYouLeaveIterator<URI>(
				new Node2uriConvertingIterator(nx, 0), br);

	}

	private File sort(File in) throws IOException, ParseException {
		_log.info("Sorting the frontier...");
		
		System.gc();
		
		InputStream is = _gzipFrontier ? new GZIPInputStream(
				new FileInputStream(in)) : new FileInputStream(in);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		File out = File.createTempFile(FILENAME_SORTED, SUFFIX);

		OutputStream os = _gzipFrontier ? new GZIPOutputStream(
				new FileOutputStream(out)) : new FileOutputStream(out);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

		// Code from NxParser's Sort class

		Iterator<Node[]> it = new NxParser(br);

		Callback cb = new CallbackNxBufferedWriter(bw, true);

		NodeComparatorArgs nca = new NodeComparatorArgs();

		// nca.setOrder(NodeComparatorArgs.getIntegerMask(cmd.getOptionValue("so")));

		// allow duplicates
		nca.setNoEquals(true);
		nca.setNoZero(true);

		NodeComparator nc = new NodeComparator(nca);

		SortArgs sa = new SortArgs(it, (short) 1);
		sa.setComparator(nc);
		sa.setGzipBatches(_gzipFrontier);

		SortIterator si = new SortIterator(sa);
		Iterator<Node[]> iter = si;

		cb.startDocument();

		while (iter.hasNext()) {
			cb.processStatement(iter.next());
		}

		cb.endDocument();

		in.delete();

		_log.info("Finished sorting the frontier. Sorted " + si.count() + " with "
				+ si.duplicates() + " duplicates.");

		return out;
	}
}
