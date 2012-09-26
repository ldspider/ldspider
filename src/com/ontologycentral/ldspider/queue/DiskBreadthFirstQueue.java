package com.ontologycentral.ldspider.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.NodeComparator.NodeComparatorArgs;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.sort.SortIterator;
import org.semanticweb.yars.nx.sort.SortIterator.SortArgs;
import org.semanticweb.yars.tld.TldManager;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;
import org.semanticweb.yars.util.Node2uriConvertingIterator;
import org.semanticweb.yars.util.PeekingIterator;
import org.semanticweb.yars.util.PleaseCloseTheDoorWhenYouLeaveIterator;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.Frontier;

/**
 * A BreadthFirstQueue on Disk.
 * 
 * @author Tobias Kaefer
 * 
 */
public class DiskBreadthFirstQueue extends RedirectsFavouringSpiderQueue {

	private static final long serialVersionUID = -7390110020717304063L;

	private static final Logger _log = Logger
			.getLogger(DiskBreadthFirstQueue.class.getName());

	private static final String BASE_TEMP_FILENAME = "ldspider-diskBreadthFirstQueueTmp";
	private static final String TEMP_FILENAME_SORTED = BASE_TEMP_FILENAME
			+ "Sorted";
	private static final String ETERNAL_BASE_TEMP_FILENAME = BASE_TEMP_FILENAME
			+ "EternalCounts";

	private static final String TEMPFILE_SUFFIX = ".nx";

	private static final String COUNT1FULLSTOP = "\"1\" .";
	private static final short _two = 2;

	public static enum CountLifeTime {
		ONE_HOP, ETERNALLY
	}

	Map<String, File> _files;
	Map<String, NxParser> _nxps;
	Set<BufferedReader> _brs;

	private final CountLifeTime _lifeTimeOfCounts;

	private boolean _isScheduled;
	private boolean _noURIsLeft;

	private File _eternalFileCounts;

	private Iterator<URI> _it4poll;

	int _minimumActivePlds;
	int _scheduledFrontiers;

	long _time;

	TldManager _tm;

	Writer _writer;

	BufferedWriter _frontierDumper;

	NodeComparator _nc;

	private int _noOfUris;

	public DiskBreadthFirstQueue(TldManager tldm, Redirects redirs,
			int minimumActivePLDs) {
		super(tldm, redirs);
		_isScheduled = false;
		_scheduledFrontiers = 0;
		_noOfUris = 0;
		_noURIsLeft = false;
		_it4poll = null;
		_nxps = null;
		_brs = new HashSet<BufferedReader>();
		_files = new HashMap<String, File>();
		_minimumActivePlds = minimumActivePLDs;

		NodeComparatorArgs nca = new NodeComparatorArgs();
		nca.setOrder(NodeComparatorArgs.getIntegerMask("10"));
		nca.setReverse(NodeComparatorArgs.getBooleanMask("1"));
		nca.setNumeric(NodeComparatorArgs.getBooleanMask("1"));
		_nc = new NodeComparator(nca);

		_lifeTimeOfCounts = CrawlerConstants.DISKBREADTHFIRSTQUEUE_COUNTLIFETIME;
		if (_lifeTimeOfCounts == CountLifeTime.ETERNALLY) {
			try {
				_eternalFileCounts = File.createTempFile(
						ETERNAL_BASE_TEMP_FILENAME, TEMPFILE_SUFFIX);
			} catch (IOException e) {
				_log.warning("could not create eternal temp file");
			}
		}
		try {
			_tm = new TldManager();
		} catch (IOException e) {
			_log.warning("No TldManager! " + e.getLocalizedMessage());
		}
	}

	private int calculateCurrentlyActivePlds() {
		int activePlds = 0;
		for (NxParser nx : _nxps.values())
			if (nx.hasNext())
				++activePlds;
		return activePlds;
	}

	@Override
	protected synchronized URI pollInternal() {
		long time0 = System.currentTimeMillis();

		if (_noURIsLeft)
			return null;

		if (!_isScheduled)
			throw new IllegalStateException("No frontier scheduled");

		if (_minimumActivePlds > -1
				&& _minimumActivePlds > calculateCurrentlyActivePlds()
				&& _scheduledFrontiers > 1) {
			_log.info("The minimum number of active PLDs has been reached. Finishing this round...");
			return null;
		}

		if (_it4poll == null)
			_it4poll = new Node2uriConvertingIterator(
					new PoliteRoundRobinIterator(_nxps.values()), 0);

		if (_it4poll.hasNext()) {
			URI next = _it4poll.next();

			--_noOfUris;
			_time = System.currentTimeMillis();

			_log.fine("poll for " + next + " done in " + (_time - time0)
					+ " ms");
			return next;
		} else {
			_log.info("last uri polled, closing this hop...");
			_noURIsLeft = true;
			_isScheduled = false;

			_nxps.clear();
			_it4poll = null;

			clear();

			return null;
		}

	}

	@Override
	public void add(URI u, boolean uriHasAlreadyBeenProcessed) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Schedules a frontier. Requires the frontier to return an iterator that
	 * traverses the URIs in a sorted manner.
	 */
	@Override
	public void schedule(Frontier f) {

		// if the previous round has not been ended by pollInternal, the data
		// structures haven't been cleaned up yet:

		clear();

		_log.info("start scheduling...");

		_time = System.currentTimeMillis();

		++_scheduledFrontiers;
		_noURIsLeft = false;
		Iterator<URI> it = f.iterator();

		if (CrawlerConstants.DUMP_FRONTIER)
			try {
				_frontierDumper = new BufferedWriter(new FileWriter(new File(
						CrawlerConstants.DUMP_FRONTIER_FILENAME + "-"
								+ (_scheduledFrontiers - 1))));
			} catch (IOException e) {
				e.printStackTrace();
			}

		_writer = new Writer();

		processFrontiersIterator(it);

		_writer.finishUp();

		if (CrawlerConstants.DUMP_FRONTIER)
			try {
				_frontierDumper.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

		Map<String, File> sortedFiles = new HashMap<String, File>(_files.size());
		if (_nxps == null)
			_nxps = new HashMap<String, NxParser>(_files.size());
		else
			_nxps.clear();

		for (Entry<String, File> e : _files.entrySet()) {
			sortedFiles
					.put(e.getKey(),
							sort(TEMP_FILENAME_SORTED + "-" + e.getKey(),
									e.getValue()));
		}

		_files.clear();
		_files = sortedFiles;

		BufferedReader br = null;
		for (Entry<String, File> e : _files.entrySet()) {
			try {
				e.getValue().deleteOnExit();
				br = new BufferedReader(new FileReader(e.getValue()));
				_nxps.put(e.getKey(), new NxParser(br));
				_brs.add(br);
			} catch (FileNotFoundException e1) {
				_log.warning(e1.getLocalizedMessage());
			} catch (Exception e3) {
				e3.printStackTrace();
			}
		}
		f.reset();
		_it4poll = null;
		_isScheduled = true;

		_log.info("scheduling " + _nxps.size() + " plds done (" + size()
				+ " URIs) in " + (System.currentTimeMillis() - _time)
				+ " ms. This was schedule No. " + _scheduledFrontiers + ".");
	}

	private void processFrontiersIterator(Iterator<URI> it) {

		Map<String, Callback> callbacks = new HashMap<String, Callback>();

		URI currentURI = null;
		URI prevURI = null;

		int currentCount = 1;

		while (it.hasNext()) {
			prevURI = currentURI;

			currentURI = it.next();

			if (prevURI != null)
				if (currentURI.equals(prevURI))
					++currentCount;
				else {
					if (!checkSeen(prevURI)) {
						++_noOfUris;
						_writer.writeOut(prevURI, currentCount);
					}
					currentCount = 1;
				}
		}

		// the last one if there was one at all:
		if (currentURI != null && !checkSeen(currentURI)) {
			++_noOfUris;
			_writer.writeOut(currentURI, currentCount);
		}

		// close all callbacks
		for (Callback c : callbacks.values()) {
			c.endDocument();
		}

		callbacks.clear();

	}

	public int size() {
		return super.size() + _noOfUris;
	}

	private class Writer {

		private final CountLifeTime _countLifeTime;
		boolean _stateFinished;

		URI _currentURI;
		URI _prevURI;

		int _currentCount;

		Map<String, Callback> _callbacks;

		PeekingIterator<Node[]> _eternal;
		File _newEternalCountsFile = null;
		Callback _newEternalCountsCB = null;

		public Writer() {
			_callbacks = new HashMap<String, Callback>();
			_countLifeTime = _lifeTimeOfCounts;
			_currentCount = 1;
			_stateFinished = false;

			if (_countLifeTime == CountLifeTime.ETERNALLY) {
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(_eternalFileCounts));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				_eternal = new PeekingIterator<Node[]>(
						new PleaseCloseTheDoorWhenYouLeaveIterator<Node[]>(
								new NxParser(br), br));
				// if (_eternal.hasNext())
				// _current = _eternal.next();
				// else
				// _eternalEmptyAndFirstUriNotYetProcessed = true;

				try {
					_newEternalCountsFile = File.createTempFile(
							ETERNAL_BASE_TEMP_FILENAME, TEMPFILE_SUFFIX);
					_newEternalCountsFile.deleteOnExit();
					_newEternalCountsCB = new CallbackNxBufferedWriter(
							new BufferedWriter(new FileWriter(
									_newEternalCountsFile)), true);
				} catch (IOException e) {
					_log.warning("Could not create new temp file for eternal counts.");
				}
			}
		}

		private void writeOut(URI u, int i) {

			if (_stateFinished)
				throw new IllegalStateException();

			if (u == null)
				return;

			if (CrawlerConstants.DUMP_FRONTIER)
				try {
					_frontierDumper.write(u.toString());
					_frontierDumper.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}

			String currentPLD = _tm.getPLD(u);

			Callback cb = null;

			if ((cb = _callbacks.get(currentPLD)) == null) {
				try {
					File file = File.createTempFile(BASE_TEMP_FILENAME + "-"
							+ currentPLD, ".nx");
					file.deleteOnExit();
					cb = new CallbackNxBufferedWriter(new BufferedWriter(
							new FileWriter(file)), true);
					_callbacks.put(currentPLD, cb);
					_files.put(currentPLD, file);
				} catch (IOException e) {
					_log.warning(e.getLocalizedMessage()
							+ " while creating file for pld "
							+ currentPLD
							+ " ("
							+ _files.size()
							+ " files for plds overall at the moment in this queue).");
					e.printStackTrace();
				}
			}

			switch (_countLifeTime) {
			case ETERNALLY:
				i = determineEternalCountAndWriteToEternal(u, i);
			default:
				cb.processStatement(new Node[] { new Resource(u),
						new Literal(Integer.toString(i)) });
				break;
			}

		}

		private int determineEternalCountAndWriteToEternal(URI u,
				int itsCountInThisRound) {
			Node[] prev = null;
			Node[] current = null;

			// empty eternal or in the previous rounds we got the last one of
			// it.
			if (_eternal.peek() == null) {
				_newEternalCountsCB.processStatement(new Node[] {
						new Resource(u),
						new Literal(Integer.toString(itsCountInThisRound)) });
				return itsCountInThisRound;
			}

			// insertion of uris before first one in eternal
			if (((Resource) _eternal.peek()[0]).toURI().compareTo(u) > 0) {
				_newEternalCountsCB.processStatement(new Node[] {
						new Resource(u),
						new Literal(Integer.toString(itsCountInThisRound)) });
				return itsCountInThisRound;
			}

			// iterate until eternal ends or uri has been put:
			while (_eternal.hasNext()) {
				prev = current;
				current = _eternal.peek();

				// should only be null if we just started off at the beginning
				// of eternal or we just went through the next if check:
				if (prev != null)
					_newEternalCountsCB.processStatement(prev);

				// if we are AT u in eternal:
				if (current != null
						&& ((Resource) current[0]).toURI().equals(u)) {
					int count = itsCountInThisRound
							+ Integer
									.parseInt(((Literal) current[1]).getData());
					current[1] = new Literal(Integer.toString(count));
					_newEternalCountsCB.processStatement(current);
					// to step forward, the object has already been peeked
					// anyway:
					_eternal.next();
					return count;
				}

				// if there is no entry for u in eternal (we just skipped over
				// its empty place):
				if (current != null
						&& ((Resource) current[0]).toURI().compareTo(u) > 0) {
					_newEternalCountsCB
							.processStatement(new Node[] {
									new Resource(u),
									new Literal(Integer
											.toString(itsCountInThisRound)) });
					return itsCountInThisRound;
				}

				// to step forward, the object has already been peeked anyway:
				_eternal.next();
			}

			// if the uri is to be put at the end of the non-empty eternal:
			_newEternalCountsCB.processStatement(current);
			_newEternalCountsCB.processStatement(new Node[] { new Resource(u),
					new Literal(Integer.toString(itsCountInThisRound)) });
			return itsCountInThisRound;
		}

		public void finishUp() {

			// the last one if there was one at all:
			if (_currentURI != null) {
				if (_prevURI != null && _currentURI.equals(_prevURI))
					++_currentCount;
				writeOut(_currentURI, _currentCount);
			}

			if (_countLifeTime == CountLifeTime.ETERNALLY) {
				// copy the rest of the old to the new eternal count list
				while (_eternal.hasNext())
					_newEternalCountsCB.processStatement(_eternal.next());
				_newEternalCountsCB.endDocument();
				_eternalFileCounts.delete();
				_eternalFileCounts = _newEternalCountsFile;
			}

			// close all callbacks
			for (Callback c : _callbacks.values()) {
				c.endDocument();
			}

			_callbacks.clear();

			_stateFinished = true;
		}
	}

	private File sort(String newBaseFileName, File in) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(in));

			// Called very often, so quick check first if there is something to
			// be sorted at all:
			br.mark(1024);

			String line1, line2, line3;

			line1 = br.readLine();
			if ((line2 = br.readLine()) == null) {
				// only <= one line in file, nothing to be sorted...
				br.close();
				_log.info("Finished sort. Sorted 1 with 0 duplicates.");
				return in;
			} else if ((line3 = br.readLine()) == null) {
				if (line1.endsWith(COUNT1FULLSTOP)
						&& line2.endsWith(COUNT1FULLSTOP)
						&& line1.compareTo(line2) < 0) {
					// only two lines, both with count 1 and already sorted
					br.close();
					_log.info("Finished sort. Sorted 2 with 0 duplicates.");
					return in;
				}
			} else if (br.readLine() == null) {
				if (line1.endsWith(COUNT1FULLSTOP)
						&& line2.endsWith(COUNT1FULLSTOP)
						&& line3.endsWith(COUNT1FULLSTOP)
						&& line1.compareTo(line2) < 0
						&& line2.compareTo(line3) < 0) {
					// only three lines, all of them with count 1 and already
					// sorted
					br.close();
					_log.info("Finished sort. Sorted 3 with 0 duplicates.");
					return in;
				}
			}
			// check done, there seems to be something to be sorted, so go on.

			// reset the data structures from the quick check:
			line1 = null;
			line2 = null;
			line3 = null;
			try {
				br.reset();
			} catch (IOException e) {
				br = new BufferedReader(new FileReader(in));
			}

			// Code from NxParser's Sort class

			File out = File.createTempFile(newBaseFileName, ".nx");
			out.deleteOnExit();

			BufferedWriter bw = new BufferedWriter(new FileWriter(out));

			Iterator<Node[]> it = new NxParser(br);

			Callback cb = new CallbackNxBufferedWriter(bw, true);

			SortArgs sa = new SortArgs(it, _two, 10000);
			sa.setComparator(_nc);
			sa.setGzipBatches(false);

			SortIterator si = null;
			try {
				si = new SortIterator(sa);
			} catch (ParseException e) {
				_log.warning(e.getLocalizedMessage());
			}
			Iterator<Node[]> iter = si;

			cb.startDocument();

			while (iter.hasNext()) {
				cb.processStatement(iter.next());
			}

			cb.endDocument();

			in.delete();

			System.gc();

			_log.info("Finished sort. Sorted " + si.count() + " with "
					+ si.duplicates() + " duplicates.");

			return out;
		} catch (IOException e) {
			_log.warning(e.getLocalizedMessage());

			return in;
		} catch (Exception e3) {
			e3.printStackTrace();
			return in;
		}
	}

	private static class PoliteRoundRobinIterator implements Iterator<Node[]> {

		Collection<NxParser> _nxparsers;

		Iterator<NxParser> _nxpIt;

		NxParser _next = null;

		boolean _firstOfNewRound;

		long _time;

		boolean _hasNextCache;
		boolean _hasNextCacheIsFresh;

		public PoliteRoundRobinIterator(Collection<NxParser> nxparsers) {
			_nxparsers = nxparsers;
			_nxpIt = nxparsers.iterator();
			_time = System.currentTimeMillis();
			_firstOfNewRound = false;
			_hasNextCache = false;
			_hasNextCacheIsFresh = false;
		}

		public boolean hasNext() {
			// So that calling hasNext() twice or more often does not
			// change the state of the iterator:
			if (!_hasNextCacheIsFresh)
				_hasNextCache = hasNextInternal();
			_hasNextCacheIsFresh = true;
			return _hasNextCache;
		}

		private boolean hasNextInternal() {
			if (_nxparsers.isEmpty())
				return false;
			boolean thereWasAfullRoundWithoutUris = false;
			boolean iStartedOverAgain = false;

			while (!thereWasAfullRoundWithoutUris) {

				while (_nxpIt.hasNext()) {
					_next = _nxpIt.next();
					if (_next.hasNext())
						return true;
				}
				if (iStartedOverAgain)
					thereWasAfullRoundWithoutUris = true;
				_nxpIt = _nxparsers.iterator();
				_firstOfNewRound = true;
				iStartedOverAgain = true;
			}
			return false;
		}

		public Node[] next() {
			_hasNextCacheIsFresh = false;
			Node[] n = _next.next();

			// Politeness:
			if (_firstOfNewRound) {
				if (System.currentTimeMillis() - _time < CrawlerConstants.MIN_DELAY) {
					_log.info("delaying queue " + CrawlerConstants.MIN_DELAY
							+ " ms ...");
					try {
						Thread.sleep(CrawlerConstants.MIN_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				_log.info("queue turnaround in "
						+ (System.currentTimeMillis() - _time) + " ms");
				_time = System.currentTimeMillis();
			}
			_firstOfNewRound = false;
			return n;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public void clear() {
		if (_nxps != null && !_nxps.isEmpty())
			_nxps.clear();

		if (_brs != null && !_brs.isEmpty())
			for (BufferedReader br : _brs)
				try {
					br.close();
				} catch (IOException e4) {
					e4.printStackTrace();
				}

		if (_files != null && !_files.isEmpty()) {
			for (File file : _files.values())
				file.delete();
			_files.clear();
		}

		_noOfUris = 0;

		System.gc();
	}

}
