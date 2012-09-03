package ie.deri.urq.lidaq.source;

import java.util.Arrays;
import java.util.logging.Logger;

import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @author Tobias Kaefer
 */
public class CallbackNQuadTripleHandler implements TripleHandler {

	Logger _log = Logger.getLogger(this.getClass().getName());

	private Callback cb;

	/**
	 * @param callback
	 */
	public CallbackNQuadTripleHandler(Callback callback) {
		cb = callback;
	}

	public void close() throws TripleHandlerException {
		;
	}

	public void closeContext(ExtractionContext arg0)
			throws TripleHandlerException {
		;
	}

	public void endDocument(URI arg0) throws TripleHandlerException {
		;
	}

	public void openContext(ExtractionContext arg0)
			throws TripleHandlerException {
		;
	}

	public void receiveNamespace(String arg0, String arg1,
			ExtractionContext arg2) throws TripleHandlerException {
		;
	}
	
	/**
	 * Receive triple in openrdf's classes, convert it to NxParser's classes
	 * and process it in the callback.
	 */
	public void receiveTriple(Resource arg0, URI arg1, Value arg2, URI arg3,
			ExtractionContext arg4) throws TripleHandlerException {
		Node subj = null, pred = null, obj = null;
		org.semanticweb.yars.nx.Resource cont = null;

		cont = convert(arg4.getDocumentURI());

		if (arg0 instanceof URI)
			subj = convert((URI) arg0);
		else if (arg0 instanceof BNode)
			subj = convert((BNode) arg0, cont);

		pred = convert(arg1);

		if (arg2 instanceof URI)
			obj = convert((URI) arg2);
		else if (arg2 instanceof Literal)
			obj = convert((Literal) arg2);
		else if (arg2 instanceof BNode)
			obj = convert((BNode) arg2, cont);

		Node[] nx = { subj, pred, obj, cont };
		for (Node n : nx)
			if (n == null) {
				throw new TripleHandlerException(
						"Error while receiving triple: " + arg0.stringValue()
								+ " " + arg1.stringValue() + " "
								+ arg2.stringValue() + " " + arg3.stringValue()
								+ " . Context was: " + arg4.getDocumentURI()
								+ " . Dropping statement.");
			}
		cb.processStatement(nx);
	}

	private org.semanticweb.yars.nx.Resource convert(URI arg0) throws TripleHandlerException{
		return new org.semanticweb.yars.nx.Resource(NxUtil.escapeForNx(arg0
				.stringValue()));
	}

	/**
	 * Converting a BNode. Context required for NxParser's BNode creation code.
	 * 
	 * @param arg0
	 *            the BNode in openrdf type hierarchy
	 * @param context
	 *            the context in which the BNode has been encountered.
	 * @return the BNode in NxParser's terms
	 */
	private org.semanticweb.yars.nx.BNode convert(BNode arg0,
			org.semanticweb.yars.nx.Resource context) throws TripleHandlerException {
		return org.semanticweb.yars.nx.BNode.createBNode(context.toN3()
				.substring(1, context.toN3().length() - 1), arg0.stringValue());
	}

	private org.semanticweb.yars.nx.Literal convert(Literal arg0) throws TripleHandlerException{
		String value = NxUtil.escapeForNx(arg0.getLabel());
		String language = null;
		org.semanticweb.yars.nx.Resource datatype = null;

		if (arg0.getDatatype() != null)
			datatype = convert(arg0.getDatatype());
		language = arg0.getLanguage();
		try {
			return new org.semanticweb.yars.nx.Literal(value, language,
					datatype);
		} catch (IllegalArgumentException e) {
			_log.warning("Something fishy in the following Literal: " + arg0
					+ " Exception caught: " + e.getMessage());
			return null;
		}
	}

	public void setContentLength(long arg0) {
		;
	}

	public void startDocument(URI arg0) throws TripleHandlerException {
		;
	}
	
//	 public static void main(String [] args) throws TripleHandlerException {
//	 Callback cb = new Callback() {
//	 public void startDocument() {;}
//	
//	 public void endDocument() {;}
//	
//	 public void processStatement(Node[] nx) {
//	 System.out.println(Arrays.toString(nx));
//	 for (Node n : nx)
//	 System.out.println(n.toN3());
//	 }
//	 };
//	
//	 TripleHandler th = new CallbackNQuadTripleHandler(cb);
//	
//	 th.receiveTriple(new BNodeImpl("subjBnode"), new URIImpl(
//	 "http://blubb.de/prädikat"), new LiteralImpl("aaääßßá",
//	 new URIImpl("http://blöbb.de/dt")), null,
//	 new ExtractionContext("bla", new URIImpl("http://blübb.de/c")));
//	
//	 }
}
