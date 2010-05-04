package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.http.Headers;

/**
 * A Sink which writes the content to a triple store using SPARQL/Update.
 * 
 * @author RobertIsele
 */
public class SinkSparul implements Sink {
	
	/** Maximum number of statements per request. */
	private int STATEMENTS_PER_REQUEST = 200;
	
	private final Logger _log = Logger.getLogger(this.getClass().getSimpleName());
	
	private final String _endpoint;

	/**
	 * @param sparulEndpoint The SPARQL/Update endpoint
	 */
	public SinkSparul(String sparulEndpoint) {
		this._endpoint = sparulEndpoint;
	}
	
	public Callback newDataset(Provenance provenance) {
		return new CallbackSparul(provenance);
	}
	
	private class CallbackSparul implements Callback {
		
		private final Provenance _prov;
		
		private HttpURLConnection _connection = null;
		
		private Writer _writer = null;
		
		private int _statements = 0;
		
		public CallbackSparul(Provenance prov) {
			if(prov == null) throw new NullPointerException("prov must not be null");
			_prov = prov;
		}
		
		public void startDocument() {
			try {
				beginSparul(true);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public void processStatement(Node[] nodes) {
			try {
				if(_statements + 1 >= STATEMENTS_PER_REQUEST ) {
					endSparql();
					beginSparul(false);
				}
				writeStatement(nodes);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		public void endDocument() {
			try {
				endSparql();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		private void beginSparul(boolean newGraph) throws IOException {
			//Preconditions
			if(_connection != null) throw new IllegalStateException("Document already openend");
			
			//Open a new HTTP connection
			URL url = new URL(_endpoint);
			_connection = (HttpURLConnection)url.openConnection();
			_connection.setRequestMethod("POST");
			_connection.setDoOutput(true);
			_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			_writer = new OutputStreamWriter(_connection.getOutputStream(), "UTF-8");
			_statements = 0;

			//SPARUL Requests	
			String provUri = URLEncoder.encode(_prov.getUri().toString(), "UTF-8");
			_writer.write("request=");
			if(newGraph) {
				_writer.write("DROP+SILENT+GRAPH+%3C" + provUri + "%3E+");
				_writer.write("CREATE+SILENT+GRAPH+%3C" + provUri + "%3E+");
			}
			_writer.write("INSERT+DATA+INTO+%3C" + provUri + "%3E+%7B");

			//Write provenance data
			Headers.processHeaders(_prov.getUri(), _prov.getHttpStatus(), _prov.getHttpHeaders(), this);
		}
		
		private void writeStatement(Node[] nodes) throws IOException {
			//Preconditions
			if(_connection == null) throw new IllegalStateException("Must open document before writing statements");
			if(nodes == null) throw new NullPointerException("nodes must not be null");
			if(nodes.length < 3) throw new IllegalArgumentException("A statement must consist of at least 3 nodes");
			
			_writer.write(URLEncoder.encode(nodes[0].toN3() + " " + nodes[1].toN3() + " " + nodes[2].toN3() + " .\n", "UTF-8"));
			_statements++;
		}
		
		private void endSparql() throws IOException {
		  //Preconditions
	    if(_connection == null) return;
			
	    //End request
			_writer.write("%7D");
			_writer.close();
			
			//Check response
			if(_connection.getResponseCode() == 200) {
				_log.info(_statements + " statements written to Store.");
			} else {
				_log.warning("Cannot write to Store. Server response: " + _connection.getResponseCode() + " " + _connection.getResponseMessage() + ".");		
			}

			_connection = null;
			_writer = null;
		}
	}
}
