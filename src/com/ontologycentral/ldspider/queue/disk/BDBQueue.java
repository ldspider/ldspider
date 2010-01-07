package com.ontologycentral.ldspider.queue.disk;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.queue.memory.FetchQueue;
import com.ontologycentral.ldspider.queue.memory.Redirects;
import com.ontologycentral.ldspider.tld.TldManager;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

public class BDBQueue extends SpiderQueue {

    private static final Logger log = Logger.getLogger(BDBQueue.class.getSimpleName());

//    private static final Integer PER_PLD_THRES = 100;

    private File envDir;
    private Environment _env;
    private EntityStore _store;
    private PrimaryIndex<String, URLObject> _urlIndex;
    private SecondaryIndex<Integer, String, URLObject> _countIndex;
//    private HashMap<String, Integer> _pldMap;
//    private List<URI> _tmpQueue;
//    private TldManager _tld;
    Redirects _redirs;

//    private long _time;

    private TldManager _tldm;

    private FetchQueue _queue;

    public BDBQueue(TldManager tldm, String queueLocation) throws URISyntaxException {
	this.envDir = new File(queueLocation);
	_tldm = tldm;
	_redirs = new Redirects();
	setup();

    }

    public void setup()
    throws DatabaseException {
	envDir.mkdirs();
	
	/* Open a transactional Berkeley DB engine environment. */
	EnvironmentConfig envConfig = new EnvironmentConfig();
	envConfig.setAllowCreate(true);
	envConfig.setTransactional(true);
	_env = new Environment(envDir, envConfig);

	/* Open a transactional entity store. */
	StoreConfig storeConfig = new StoreConfig();
	storeConfig.setAllowCreate(true);
	storeConfig.setTransactional(true);
	_store = new EntityStore(_env, "BDBQueue", storeConfig);

	/* Primary index of the Employee database. */
	_urlIndex = _store.getPrimaryIndex(String.class, URLObject.class);

	/* Secondary index of the Employee database. */
	_countIndex = _store.getSecondaryIndex(_urlIndex,
		Integer.class,
	"count");
//	_pldMap = new HashMap<String, Integer>(); 
//	_tmpQueue = new ArrayList<URI>();
    }


    public boolean close() {
	boolean success = true;
	if (_store != null) {
	    try {
		_store.close();
	    } catch (DatabaseException dbe) {
		System.err.println("Error closing store: " +
			dbe.toString());
		success = false;
	    }
	}
	if (_env != null) {
	    try {
		// Finally, close environment.
		_env.close();
	    } catch (DatabaseException dbe) {
		System.err.println("Error closing env: " +
			dbe.toString());
		success = false;
	    }
	}
	return success;
    }

    @Override
    public void addFrontier(URI url){
	String uri;
	try {
	    uri = normalise(url).toASCIIString();
	    synchronized (_urlIndex) {
		    URLObject o = _urlIndex.get(uri);
		    if(o == null) _urlIndex.put(new URLObject(uri));
		    else if(o.getCount()==-1) return;
		    else{
			o.incrementCount();
			_urlIndex.put(o);
		    }
		}
	} catch (URISyntaxException e) {
	    // TODO Auto-generated catch block
//	    e.printStackTrace();
	    return;
	}
	
    }

    @Override
    public URI obtainRedirect(URI from) {
	URI to = _redirs.getRedirect(from);
	if (from != to) {
		log.info("redir from " + from + " to " + to);
		seen(to);
		return to;
	}
	
	return from;
    }

    @Override
    public URI poll() {
	// TODO Auto-generated method stub
	return _queue.poll();
    }

    @Override
    public void schedule(int maxuris) {
	_queue = new FetchQueue(_tldm);
	
	log.info("Schedule new queue");
//	_pldMap = new HashMap<String, Integer>();
	EntityCursor<URLObject> cursor  = _countIndex.entities();
	URLObject o = null; 
	int counter = 0;
	do{
	    o = cursor.prev();
	    if(o!=null && o.getCount()!=-1){
		_queue.addFrontier(o.getURI());
		counter++;
	    }
	}while(counter!=maxuris && o != null);
	_queue.schedule(maxuris);
//	log.info("New Queue contains: "+_tmpQueue.size()+" elements from "+_pldMap.size()+" plds");
//	Collections.shuffle(_tmpQueue);
//	log.info("Shuffled");
	cursor.close();
	
    }

    @Override
    public void setRedirect(URI from, URI to) {
	try {
		to = normalise(to);
	} catch (URISyntaxException e) {
		log.info(to +  " not parsable, skipping " + to);
		return;
	}
	
	if (from.equals(to)) {
		log.info("redirected to same uri " + from);
		return;
	}
	
	if (_redirs.put(from, to) == true) {
		// allow to poll from again from queue
		unseen(from);
	
		// fetch again, this time redirects are taken into account
		_queue.addDirectly(from);
	}
	
    }

//    //    @Override
//    //    public boolean hasNext() throws IOException {
//    //	return !_tmpQueue.isEmpty();
//    //    }
//
//
//    public  synchronized URI poll()  {
//	if (_tmpQueue == null) {
//	    return null;
//	}
//
//	URI next = null;
//
//	int empty = 0;
//
//	do {	
//	    if (_tmpQueue.isEmpty()) {
//		return null;
//	    }
//	    
//	    String pld = _current.poll();
//	    Queue<URI> q = _queues.get(pld);
//
//	    if (q != null && !q.isEmpty()) {
//		next = q.poll();
//		if (getSeen(next)) {
//		    _log.info("get seen true for " + next);
//		    next = null;
//		} else {
//		    setSeen(next);
//		}
//	    } else {
//		empty++;
//	    }
//	} while (next == null && empty < _queues.size());
//
//	return next;
//	if(_tmpQueue.isEmpty()){
//	    schedule();  
//	}    
//	return _tmpQueue.remove(0);
//    }
//
//    //    public  void schedule() {
//    //	
//    //    }
//
//
//    public void printQueue() {
//	EntityCursor<URLObject> cursor  = _countIndex.entities();
//	URLObject o = null; 
//	do{
//	    o = cursor.prev();
//	    //	    System.out.println(o);
//	    if(o!=null)
//		System.out.println(o);
//	}while(o != null);
//	cursor.close();
//    }
//
//
//    private void addToQueue(URLObject o) {
//	//check the pld
//	String pld;
//	pld = _tld.getPLD(o.getURI());
//	Integer count = _pldMap.get(pld);
//	if(count == null) count = 0;
//	else if(count > PER_PLD_THRES) return;
//
//	//change value
//	//	    o.setTBCrawled();
//	//	    _urlIndex.put(o);
//	_tmpQueue.add(o.getURI());
//	_pldMap.put(pld, ++count);
//
//    }
//
//    //    @Override
//    //    public Stack<String> poll(int n) throws IOException {
//    //	Stack<String> s = new Stack<String>();
//    //	for (int i=0; i < n; i++) {
//    //	    String uri = poll();
//    //	    if (uri != null)
//    //		s.add(uri);
//    //	}
//    //	if(s.isEmpty()) 
//    //	    return null;
//    //	return s;
//    //    }
//
//    void removeDbFiles() {
//
//	for (File f : envDir.listFiles()) {
//	    f.delete();
//	}
//    }
//
    public synchronized void seen(URI url) {
	URLObject o = _urlIndex.get(url.toASCIIString());
	if(o==null)
	    o = new URLObject(url.toASCIIString());
	o.setTBCrawled();
	_urlIndex.put(o);
    }
    
    public synchronized void unseen(URI url) {
	URLObject o = _urlIndex.get(url.toASCIIString());
	if(o==null)
	    o = new URLObject(url.toASCIIString());
	o.setTBCrawledAgain();
	_urlIndex.put(o);
    }
//
//    public long size(){
//	return _urlIndex.count();
//    }
//
//    public void addFrontier(URI url){
//	synchronized (_urlIndex) {
//	    URLObject o = _urlIndex.get(url);
//	    if(o == null) _urlIndex.put(new URLObject(url));
//	    else if(o.getCount()==-1) return;
//	    else{
//		o.incrementCount();
//		_urlIndex.put(o);
//	    }
//	}
//    }
//
//    public void addFrontier(Collection<URI> uris){
//	for(URI s: uris){
//	    addFrontier(s);
//	}
//    }
//
//
//    /**
//     * Return redirected URI (if there's a redirect)
//     * otherwise return original URI.
//     * 
//     * @param from
//     * @return
//     */
//    public URI obtainRedirect(URI from) {
//	URI to = _redirs.getRedirect(from);
//	if (from != to) {
//	    log.info("redir from " + from + " to " + to);
//	    seen(to);
//	    return to;
//	}
//
//	return from;
//    }
//
//    @Override
//    public void schedule(int maxuris) {
//	log.info("Schedule new queue");
//	_pldMap = new HashMap<String, Integer>();
//	EntityCursor<URLObject> cursor  = _countIndex.entities();
//	URLObject o = null; 
//	do{
//	    o = cursor.prev();
//	    if(o!=null)
//		addToQueue(o);
//	}while(_tmpQueue.size()!=maxuris && o != null);
//	log.info("New Queue contains: "+_tmpQueue.size()+" elements from "+_pldMap.size()+" plds");
//	Collections.shuffle(_tmpQueue);
//	log.info("Shuffled");
//	cursor.close();
//
//    }
//
//    /**
//     * Set the redirect.
//     */
//    public void setRedirect(URI from, URI to) {
//	try {
//	    to = normalise(to);
//	} catch (URISyntaxException e) {
//	    log.info(to +  " not parsable, skipping " + to);
//	    return;
//	}
//
//	if (from.equals(to)) {
//	    log.info("redirected to same uri " + from);
//	    return;
//	}
//
//	if (_redirs.put(from, to) == true) {
//	    // allow to poll from again from queue
//	    _seen.remove(from);
//
//	    // fetch again, this time redirects are taken into account
//	    _tmpQueue.add(from);
//	}
//    }

    @Override
    public int size() {
	return _queue.size();
    }
}

@Entity
class URLObject {

    @PrimaryKey
    String url;

    /* Many Employees may have the same name. */
    @SecondaryKey(relate = MANY_TO_ONE)
    int count;

    long timestamp;

    private int weight;

    public URLObject(String url) {
	this.url = url;
	count=1;
    }

    public void setTBCrawled() {
	weight = count;
	count = -1;	

    }

    private URLObject() {} // Needed for deserialization.

    public URI getURI(){
	try {
	    return new URI(url);
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public int getCount(){
	return count;
    }

    public void incrementCount(){
	count++;
    }

    public void setTBCrawledAgain() {
	count = weight;	

    }
    
    public long getTimestamp(){
	return timestamp;
    }

    @Override
    public String toString() {
	if(weight!=0)
	    return this.url + " weight:"+this.weight+" count:"+this.count;
	else
	    return this.url + " weight:"+this.count;
    }
}
