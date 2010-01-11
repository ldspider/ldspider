package com.ontologycentral.ldspider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackNQOutputStream;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;

public class Main {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());


	public static void main(String[] args) {
		Options options = new Options();

		OptionGroup input = new OptionGroup();

		Option seeds  = OptionBuilder.withArgName( "seed list")
		.hasArgs(1)
		.withDescription( "location of the seed list" )
		.create( "s" );
		seeds.setRequired(true);
		input.addOption(seeds);

		Option uri  = OptionBuilder.withArgName( "uri")
		.hasArgs(1)
		.withDescription( "uri of an instance" )
		.create( "u" );
		uri.setRequired(true);
		input.addOption(uri);
		options.addOptionGroup(input);

		Option threads = OptionBuilder.withArgName( "threads")
		.hasArgs(1)
		.withDescription( "number of threads (default "+CrawlerConstants.DEFAULT_NB_THREADS+")")
		.create( "t" );
		options.addOption(threads);

		Option rounds = OptionBuilder.withArgName( "depth")
		.hasArgs(1)
		.withDescription( "depth; number of rounds (default "+CrawlerConstants.DEFAULT_NB_ROUNDS+")")
		.create( "d" );
		options.addOption(rounds);

		Option redirs = OptionBuilder.withArgName( "redirects")
		.hasArgs(1)
		.withDescription( "write redirects.nx file")
		.create( "r" );
		options.addOption(redirs);

		Option output  = OptionBuilder.withArgName( "file name")
		.hasArgs(1)
		.withDescription( "name of NQ file with output " )
		.create( "o" );
		options.addOption(output);

		Option log = OptionBuilder.withArgName("log file name")
		.hasArgs(1)
		.withDescription( "name of access log file" )
		.create( "l" );
		options.addOption(log);

		Option maxuris  = OptionBuilder.withArgName( "max no uris")
		.hasArgs(1)
		.withDescription( "max no of uris per pld per round" )
		.create( "m" );
		options.addOption(maxuris);
		
		Option ondisk  = OptionBuilder.withArgName( "on-disk queue")
		.hasArgs(0)
		.withDescription( "use the BDB on-disk queue with URI selection based on their frequency." )
		.create( "b" );
		options.addOption(ondisk);

//		Option useragent  = OptionBuilder.withArgName( "user agent")
//		.hasArgs(1)
//		.withDescription( "user agent" )
//		.create( "a" );
//		options.addOption(useragent);
		
//		Option error  = OptionBuilder.withArgName( "error")
//		.hasArgs(1)
//		.withDescription( "error log file" )
//		.create( "e" );
//		input.addOption(error);


		Option help0 = new Option("h", "help",false,"print help");
		options.addOption(help0);

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args,true);
			if (cmd.hasOption("h")&& cmd.hasOption("help")) {
				formatter.printHelp(80," ","Life lookups on the linked data web\n", options,"\nFeedback and comments are welcome",true );
				System.exit(0);
			} else if (!cmd.hasOption("s") && !cmd.hasOption("u")) {
				formatter.printHelp(80," ","ERROR: Missing required option: s or u \n", options,"\nError occured! Please see the error message above",true );
				System.exit(-1);    
			}

			run(cmd);
		} catch (org.apache.commons.cli.ParseException e) {
			formatter.printHelp(80," ","ERROR: "+e.getMessage()+"\n", options,"\nError occured! Please see the error message above",true );
			System.exit(-1);
		} catch (FileNotFoundException e) {
			formatter.printHelp(80," ","ERROR: "+e.getMessage()+"\n", options,"\nError occured! Please see the error message above",true );
			System.exit(-1);
		}
		catch (NumberFormatException e) {
			formatter.printHelp(80," ","ERROR: "+e.getMessage()+"\n", options,"\nError occured! Please see the error message above",true );
			System.exit(-1);
		}
	}

	private static void run(CommandLine cmd) throws FileNotFoundException {
		//check seed file
		Set<URI> seeds = null;
		if(cmd.hasOption("s")){
			File seedList = new File(cmd.getOptionValue("s"));
			if(!seedList.exists()) throw new FileNotFoundException("No file found at "+seedList.getAbsolutePath());
			seeds = readSeeds(seedList);
		}else if(cmd.hasOption("u")){
			seeds = new HashSet<URI>();
			try {
				seeds.add(new URL(cmd.getOptionValue("u").trim()).toURI());
			} catch (Exception e) {
				_log.log(Level.WARNING,"Discard invalid uri "+e.getMessage()+" for "+cmd.hasOption("u"));
				e.printStackTrace();
				System.exit(-1);
			}
		}

		int depth = CrawlerConstants.DEFAULT_NB_ROUNDS;
		int threads = CrawlerConstants.DEFAULT_NB_THREADS;
		int maxuris = CrawlerConstants.DEFAULT_NB_URIS;
		
		if(cmd.hasOption("d")) 
			depth = Integer.valueOf(cmd.getOptionValue("d"));
		if(cmd.hasOption("t")) 
			threads = Integer.valueOf(cmd.getOptionValue("t"));
		if(cmd.hasOption("m")) 
			maxuris = Integer.valueOf(cmd.getOptionValue("m"));

		_log.info("crawling with " + threads + " threads, maxuris " + maxuris + " depth " + depth);
		_log.info("seed uri " + seeds);
		
		//start the crawl
		long time = System.currentTimeMillis();
		
		OutputStream os = System.out;
		
		if (cmd.hasOption("o")) {
			os = new FileOutputStream(cmd.getOptionValue("o"));
		}
		
		Crawler c = new Crawler(threads);
		
		PrintStream ps = System.out;
		
		if (cmd.hasOption("l")) {
			ps = new PrintStream(new FileOutputStream(cmd.getOptionValue("l")));			
		}
		
		Callback rcb = null;
		if (cmd.hasOption("r")) {
			FileOutputStream fos = new FileOutputStream(cmd.getOptionValue("r"));
			rcb = new CallbackNQOutputStream(fos);
			rcb.startDocument();
		}

		ErrorHandler eh = new ErrorHandlerLogger(ps, rcb);
		c.setErrorHandler(eh);
		c.setOutputCallback(new CallbackNQOutputStream(os));
		c.setLinkSelectionCallback(new LinkFilterDefault(eh));
		c.setFetchFilter(new FetchFilterRdfXml(eh));
		
		if (cmd.hasOption("b")) {
			c.evaluate(seeds, depth, maxuris, cmd.getOptionValue("b"));
		} else {
			c.evaluate(seeds, depth, maxuris);
		}

		System.err.println(eh);
//		for (Throwable t : eh.getErrors()) {
//			System.err.println(t.getMessage());		// TODO Auto-generated method stub

//		}
		
		c.close();

		long time1 = System.currentTimeMillis();
		
		try {
			os.close();
		} catch (IOException e) {
			_log.log(Level.WARNING, "could not close output stream: " + e.getMessage());
		}
		
		if (rcb != null) {
			rcb.endDocument();
		}

		System.err.println("time elapsed " + (time1-time) + " ms " + (float)eh.lookups()/((time1-time)/1000.0) + " lookups/sec" );
	}

	/**
	 * 
	 * @param q - queue
	 * @param seedList
	 * @throws FileNotFoundException - should never happen since the check was done in method before
	 */
	private static Set<URI> readSeeds(File seedList) throws FileNotFoundException {
		Set<URI> seeds = new HashSet<URI>();
		
		Scanner s = new Scanner(seedList);
		String line=null;
		URL uri = null;
		while(s.hasNextLine()){
			line = s.nextLine().trim();
			try {
				uri = new URL(line);
				seeds.add(uri.toURI());	
			} catch (URISyntaxException e) {
				_log.log(Level.FINE,"Discard invalid uri "+e.getMessage()+" for "+line);
			} catch (MalformedURLException e) {
				_log.log(Level.FINE,"Discard invalid uri "+e.getMessage()+" for "+line);
			}
		}
		
		return seeds;
	}
}
