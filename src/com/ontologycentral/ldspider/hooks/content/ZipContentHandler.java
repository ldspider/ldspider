package com.ontologycentral.ldspider.hooks.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Stores content in ZIP files
 * 
 * @author aharth
 */
public class ZipContentHandler implements ContentHandler {
	private final Logger _log = Logger.getLogger(this.getClass().getName());

	File _dir;
	ZipOutputStream _zip;
	int _i = 0;
	
	public static int MAX_FILES = 512;
	public static int BUF_SIZE = 4096;
	
	public ZipContentHandler(File dir) throws FileNotFoundException {
		_dir = dir;
		
		if (!_dir.exists()) {
			if (_dir.mkdir() == false) {
				_log.severe("cannot create directory " + _dir.toString());
			}
		}
		
		_zip = new ZipOutputStream(new FileOutputStream(new File(_dir, "archive" + _i / MAX_FILES + ".zip")));
		_i++;
	}
	
	public boolean canHandle(String mime) {
		return true;
	}
	
	public void close() throws IOException {
		_zip.close();
	}
	
	public synchronized boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		try {
			if (_i > 0 && _i % MAX_FILES == 0) {
				_zip.close();
				_zip = new ZipOutputStream(new FileOutputStream(new File(_dir, "archive" + _i / MAX_FILES + ".zip")));
			}
			_i++;
			
			ZipEntry ze = new ZipEntry(URLEncoder.encode(uri.toString(), "utf-8"));
			_zip.putNextEntry(ze);

			int read;
			byte[] buf = new byte[BUF_SIZE];
			while ( (read = source.read(buf) ) >= 0) {
				_zip.write(buf, 0, read);
			}
			_zip.closeEntry();
		} catch (Exception e) {
			_log.info(e.getMessage());
			return false;
		}
		return true;
	}

	public String[] getMimeTypes() {
		return new String[0];
	}
}
