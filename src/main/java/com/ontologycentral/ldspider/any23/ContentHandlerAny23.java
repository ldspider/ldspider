package com.ontologycentral.ldspider.any23;

import ie.deri.urq.lidaq.source.CallbackNQuadTripleHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractorFactory;
import org.deri.any23.extractor.ExtractorGroup;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.extractor.html.TurtleHTMLExtractor;
import org.deri.any23.extractor.rdf.NQuadsExtractor;
import org.deri.any23.extractor.rdf.NTriplesExtractor;
import org.deri.any23.extractor.rdf.RDFXMLExtractor;
import org.deri.any23.extractor.rdf.TurtleExtractor;
import org.deri.any23.extractor.rdfa.RDFaExtractor;
import org.deri.any23.filter.IgnoreTitlesOfEmptyDocuments;
import org.deri.any23.http.AcceptHeaderBuilder;
import org.deri.any23.mime.MIMEType;
import org.deri.any23.source.ByteArrayDocumentSource;
import org.deri.any23.writer.TripleHandler;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.http.Headers;
import com.ontologycentral.ldspider.http.Headers.Treatment;

/**
 * A Content Handler that uses Any23 (not via server).
 * 
 * @author Tobias Kaefer
 * 
 */
public class ContentHandlerAny23 implements ContentHandler {

	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	private final Any23 runner;
	private final TripleHandler headerTripleHandler;
	private final Headers.Treatment headerTreatment;

	private final String[] mimetypesStrings;
	private final ExtractorGroup extractorGroup;
	private final Collection<MIMEType> mimeTypes;

	public static String[] getDefaultExtractorNames() {
		String[] extractorNames = { RDFaExtractor.NAME,
				RDFXMLExtractor.factory.getExtractorName(),
				TurtleExtractor.factory.getExtractorName(),
				NTriplesExtractor.factory.getExtractorName(),
				NQuadsExtractor.factory.getExtractorName(),
				TurtleHTMLExtractor.NAME };
		return extractorNames;
	}

	public ContentHandlerAny23() {
		// causes any23 to use all extractors
		this((String) null);
	}

	public ContentHandlerAny23(String... extractorNames) {
		this(null, Treatment.INCLUDE, extractorNames);
	}

	public ContentHandlerAny23(TripleHandler headerTripleHandler,
			Treatment headerTreatment, String... extractorNames) {
		this.headerTripleHandler = headerTripleHandler;
		runner = new Any23(extractorNames);
		this.headerTreatment = headerTreatment;

		extractorGroup = ExtractorRegistry.getInstance().getExtractorGroup(
				Arrays.asList(extractorNames));
		mimeTypes = new LinkedList<MIMEType>();
		for (ExtractorFactory<?> ef : extractorGroup)
			mimeTypes.addAll(ef.getSupportedMIMETypes());
		mimetypesStrings = determineMimeTypes(extractorNames);
	}

	/**
	 * Get the mime types for the any23 extractor names supplied.
	 * @param extractorNames
	 * @return mime types
	 */
	private String[] determineMimeTypes(String[] extractorNames) {
		Map<String,Double> mimeAndQ = new HashMap<String,Double>();
		Collection<MIMEType> mimetypes;
		Double d;
		for (ExtractorFactory<?> ef : extractorGroup) {
			mimetypes = ef.getSupportedMIMETypes();
			// get maximum quality for each mimetype
			for (MIMEType mt : mimetypes)
				if ((d = mimeAndQ.get(mt.getFullType())) == null) {
					mimeAndQ.put(mt.getFullType(), mt.getQuality());
				} else if (d < mt.getQuality())
					mimeAndQ.put(mt.getFullType(), mt.getQuality());
		}

		// convert to list for sorting
		List<Entry<String, Double>> mimeAndQualityList = new ArrayList<Entry<String, Double>>(
				mimeAndQ.size());
		mimeAndQualityList.addAll(mimeAndQ.entrySet());

		// sort
		Collections.sort(mimeAndQualityList,
				new Comparator<Entry<String, Double>>() {
					public int compare(Entry<String, Double> arg0,
							Entry<String, Double> arg1) {
						int val = -arg0.getValue().compareTo(arg1.getValue());
						if (val != 0)
							return val;
						else
							return arg0.getKey().compareTo(arg1.getKey());
					}
				});

		// prepare for conversion to String[]
		ArrayList<String> mimeAndQualityStringList = new ArrayList<String>();
		for (Entry<String, Double> entry : mimeAndQualityList)
			if (entry.getValue().doubleValue() == 1.0)
				mimeAndQualityStringList.add(entry.getKey());
			else
				mimeAndQualityStringList.add(entry.getKey() + ";q="
						+ entry.getValue());
		
		return mimeAndQualityStringList
				.toArray(new String[mimeAndQualityStringList.size()]);
	}

	/**
	 * Tells whether this content handler can handle the mime type provided.
	 * Note that Any23 guesses the type of the file it in the processing anyway.
	 */
	public boolean canHandle(String mime) {
		try {
		return !extractorGroup.filterByMIMEType(MIMEType.parse(mime)).isEmpty();
		} catch (IllegalArgumentException e) {
			_log.warning(e.getMessage());
			return false;
		}
	}

	public String[] getMimeTypes() {
		return mimetypesStrings;
	}

	/**
	 * Takes the mime types for this ContentHandler and the ones supplied and
	 * composes a string for the accept header from it.
	 * 
	 * @param moreMimeTypes
	 * @return a string for the HTTP accept header
	 */
	public String composeAcceptHeader(String... moreMimeTypes) {
		Collection<MIMEType> mimetypes = new LinkedList<MIMEType>();
		mimetypes.addAll(mimeTypes);
		for (String s : moreMimeTypes)
			try {
				mimetypes.add(MIMEType.parse(s));
			} catch (IllegalArgumentException e) {
				_log.warning(e.getMessage());
				continue;
			}
		return new AcceptHeaderBuilder(mimetypes).getAcceptHeader();
	}

	public boolean handle(URI uri, String mime, InputStream source,
			Callback callback) {
		try {
			if (headerTreatment == Treatment.DROP)
			
			runner.extract(
					new ByteArrayDocumentSource(source, uri.toASCIIString(),
							mime), new IgnoreOrDumpAny23addedSindiceStuff(
							headerTripleHandler, false,
							new IgnoreAccidentalRDFaReally(
									new IgnoreTitlesOfEmptyDocuments(
											new CallbackNQuadTripleHandler(
													callback)))));
			else if (headerTreatment == Treatment.DUMP)
				runner.extract(
						new ByteArrayDocumentSource(source, uri.toASCIIString(),
								mime), new IgnoreOrDumpAny23addedSindiceStuff(
								headerTripleHandler, true,
								new IgnoreAccidentalRDFaReally(
										new IgnoreTitlesOfEmptyDocuments(
												new CallbackNQuadTripleHandler(
														callback)))));
			else // headerTreatment == Treatment.INCLUDE
				runner.extract(
						new ByteArrayDocumentSource(source,
								uri.toASCIIString(), mime),
						new IgnoreAccidentalRDFaReally(
								new IgnoreTitlesOfEmptyDocuments(
										new CallbackNQuadTripleHandler(callback))));

			return true;
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read document " + uri, e);
			return false;
		} catch (Exception e) {
			_log.log(Level.WARNING, "Could not read document " + uri, e);
			return false;
		}
	}
}
