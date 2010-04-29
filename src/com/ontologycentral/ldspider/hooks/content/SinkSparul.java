package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
			//Preconditions
			if(_connection != null) throw new IllegalStateException("Document already openend");
			
			try {

				//Open a new HTTP connection
				URL url = new URL(_endpoint);
				_connection = (HttpURLConnection)url.openConnection();
				_connection.setRequestMethod("POST");
				_connection.setDoOutput(true);
				_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				_writer = new OutputStreamWriter(_connection.getOutputStream(), "UTF-8");

				//Begin SPARQL Request			
				_writer.write("request=CREATE+SILENT+GRAPH+%3C" + _prov.getUri().toASCIIString() + "%3E+");
				_writer.write("INSERT+DATA+INTO+%3C" + _prov.getUri().toASCIIString() + "%3E+%7B");
				_statements = 0;

				//Write provenance data
				Headers.processHeaders(_prov.getUri(), _prov.getHttpStatus(), _prov.getHttpHeaders(), this);

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public void processStatement(Node[] nodes) {
			//Preconditions
			if(_connection == null) throw new IllegalStateException("Must open document before writing statements");
			if(nodes == null) throw new NullPointerException("nodes must not be null");
			if(nodes.length < 3) throw new IllegalArgumentException("A statement must consist of at least 3 nodes");
		
			//Write statement
			try {
				_writer.write(URLEncoder.encode(nodes[0].toN3() + " " + nodes[1].toN3() + " " + nodes[2].toN3() + " .\n", "UTF-8"));
				_statements++;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		public void endDocument() {
		  //Preconditions
	    if(_connection == null) return;
			
			try {
			  //End SPARQL Request
				_writer.write("%7D");
				_writer.close();

				_log.info("New graph with " + _statements + " statements written to Store. " + 
						"Server response: " + _connection.getResponseCode() + " " + _connection.getResponseMessage() + ".");
				
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
			_connection = null;
			_writer = null;
		}
	}
}
