package com.ontologycentral.ldspider;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.ontologycentral.ldspider.hooks.sink.TakingHopsIntoAccount;

/**
 * A class to write stuff on disk. Can split the output into numbered files
 * (e.g. one for each hop).
 * 
 * @author Tobias Kaefer
 */
public class HopwiseSplittingFileOutputter implements Appendable,
		TakingHopsIntoAccount, Closeable {

	static Logger _log = Logger
			.getLogger(HopwiseSplittingFileOutputter.class.getName());

	enum State {
		ACCEPTING, NOT_ACCEPTING
	}

	String _basefilename;
	String _basefileextension;

	int _hop;

	Closeable _clo;
	Appendable _app;

	State _state = State.NOT_ACCEPTING;

	/**
	 * Whether hop n's data should also be in the file for hop n + 1.
	 */
	final boolean _retain;

	public HopwiseSplittingFileOutputter(String fileName)
			throws IOException {
		this(fileName, 0);
	}

	public HopwiseSplittingFileOutputter(String fileName, int initialHop)
			throws IOException {
		this(fileName, initialHop, false);
	}

	public HopwiseSplittingFileOutputter(String fileName,
			boolean retainLastHopsDataInNewHop) throws IOException {
		this(fileName, 0, retainLastHopsDataInNewHop);
	}

	public HopwiseSplittingFileOutputter(String fileName, int initialHop,
			boolean retainLastHopsDataInNewHop) throws IOException {

		String[] fnameAndExtension = Util.determineFnameAndExtension(fileName);

		PrintStream ps = new PrintStream(
				Util.createBufferedOutputStreamForHopwiseOperation(
						fnameAndExtension[0], fnameAndExtension[1], initialHop));

		_basefilename = fnameAndExtension[0];
		_basefileextension = fnameAndExtension[1];
		_hop = initialHop;
		_clo = ps;
		_app = ps;
		_retain = retainLastHopsDataInNewHop;

		CrawlerConstants.THOSE_WHO_TAKE_HOPS_INTO_ACCOUNT.add(this);

		_state = State.ACCEPTING;
	}

	public void nextHop() throws IOException {
		this.nextHop(_hop + 1);
	}

	public void nextHop(int hop) throws IOException {
		_log.info("Preparing " + this.getClass().getSimpleName()
				+ " for files named like " + _basefilename + "."
				+ _basefileextension + " for next hop #" + hop);

		_state = State.NOT_ACCEPTING;

		_hop = hop;

		_clo.close();

		CrawlerConstants.CLOSER.remove(_clo);

		PrintStream ps = new PrintStream(
				Util.createBufferedOutputStreamForHopwiseOperation(
						_basefilename, _basefileextension, hop));

		_clo = ps;
		_app = ps;

		if (_retain) {
			File file = new File(Util.createFileNameForHopwiseOperation(
					_basefilename, _basefileextension, hop - 1));
			if (file.exists()) {
				// copying data from the old file to the new file

				_log.info("copying between hop files: "
						+ Util.createFileNameForHopwiseOperation(_basefilename,
								_basefileextension, hop - 1)
						+ " to "
						+ Util.createFileNameForHopwiseOperation(_basefilename,
								_basefileextension, hop));

				InputStream is;

				if (_basefileextension.equals("gz"))
					is = new GZIPInputStream(new FileInputStream(file));
				else
					is = new BufferedInputStream(new FileInputStream(file));

				int data = -1;
				while ((data = is.read()) > -1)
					ps.write(data);

				is.close();
			} else
				_log.info("I should have retained the input from the old "
						+ "file to the new file, but the file did not exist: "
						+ file.getPath());

		}

		_state = State.ACCEPTING;
		_log.info(this.getClass().getSimpleName() + " prepared.");
	}

	public Appendable append(CharSequence csq) throws IOException {
		if (_state != State.ACCEPTING)
			throw new IllegalStateException();
		synchronized (this) {
			_app.append(csq);
		}
		return this;
	}

	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		if (_state != State.ACCEPTING)
			throw new IllegalStateException();
		synchronized (this) {
			_app.append(csq, start, end);
		}
		return this;
	}

	public Appendable append(char c) throws IOException {
		if (_state != State.ACCEPTING)
			throw new IllegalStateException();
		synchronized (this) {
			_app.append(c);
		}
		return this;
	}
	
	public void close() throws IOException {
		_clo.close();
		CrawlerConstants.CLOSER.remove(_clo);
		_state = State.NOT_ACCEPTING;
	}
}
