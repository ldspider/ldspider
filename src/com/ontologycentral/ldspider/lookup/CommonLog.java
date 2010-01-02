package com.ontologycentral.ldspider.lookup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write common log format
 * 
 */
public class CommonLog {
	private BufferedWriter _logger;

	//[10/Oct/2000:13:55:36 -0700]
	public final SimpleDateFormat APACHEDATEFORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

	public CommonLog(String fname) throws IOException {
		_logger = new BufferedWriter(new FileWriter(new File(fname)));
	}
	
	public CommonLog() {
		_logger = null;
	}


	public void close() throws IOException {
		if(_logger != null){
			_logger.close();
		}
	}
	
	public String getDateAsISO8601String(Date date) {
		String result = APACHEDATEFORMAT.format(date);
		//convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
		//- note the added colon for the Timezone
		return result;
	}

	public String getDateAsISO8601String() {
		return getDateAsISO8601String(new Date());
	}

	/**
	 * NOTE!
	 * format : REQUESTED_DOMAIN - - [DATE] "REQUEST_METHOD REQUEST_FILE HTTP" RESPCODE CONTENT_LENGTH
	 *  RESPCODE: the normal HTTP respcodes , additionally MC_HTTP RespCodes 
	 *  CONTENT_LENGTH: -1 if the content.length is not available
	 * @param uriDoc
	 * @throws IOException 
	 */
	public synchronized void common(String host, String file, int respCode, long contentLength) throws IOException {
		if (_logger != null) {
			StringBuilder sb = new StringBuilder();

			//127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
			sb.append(host);
			sb.append(" - - [");
			sb.append(getDateAsISO8601String());
			sb.append("] ");
			sb.append("\"GET ");
			sb.append(file);
			sb.append(" HTTP\" ");
			sb.append(respCode);
			sb.append(" ");
			sb.append(contentLength);
			sb.append("\n");

			_logger.write(sb.toString());
			_logger.flush();
		}
	}
}
