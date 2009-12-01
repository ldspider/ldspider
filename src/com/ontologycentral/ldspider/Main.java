package com.ontologycentral.ldspider;

import java.io.File;
import java.io.FileNotFoundException;
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
		.create( "uri" );
		uri.setRequired(true);
		input.addOption(uri);
		options.addOptionGroup(input);

		Option threads = OptionBuilder.withArgName( "threads")
		.hasArgs(1)
		.withDescription( "number of threads (default "+CrawlerConstants.DEFAULT_NB_THREADS+")")
		.create( "t" );
		options.addOption(threads);

		Option rounds = OptionBuilder.withArgName( "rounds")
		.hasArgs(1)
		.withDescription( "number of rounds (default "+CrawlerConstants.DEFAULT_NB_ROUNDS+")")
		.create( "r" );
		options.addOption(rounds);


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
			}else if(!cmd.hasOption("s") && !cmd.hasOption("uri")){
				formatter.printHelp(80," ","ERROR: Missing required option: s or uri \n", options,"\nError occured! Please see the error message above",true );
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
		}else if(cmd.hasOption("uri")){
			seeds = new HashSet<URI>();
			try {
				seeds.add(new URL(cmd.getOptionValue("uri").trim()).toURI());
			} catch (Exception e) {
				_log.log(Level.WARNING,"Discard invalid uri "+e.getMessage()+" for "+cmd.hasOption("uri"));
				System.exit(-1);
			}
		}

		int rounds = CrawlerConstants.DEFAULT_NB_ROUNDS;
		int threads = CrawlerConstants.DEFAULT_NB_THREADS;
		if(cmd.hasOption("r")) 
			rounds = Integer.valueOf(cmd.getOptionValue("r"));
		if(cmd.hasOption("t")) 
			threads = Integer.valueOf(cmd.getOptionValue("t"));

		//start the crawl
		long time = System.currentTimeMillis();

		
		Crawler c = new Crawler();

		ErrorHandler eh = new ErrorHandlerLogger();
		c.setErrorHandler(eh);
		c.setOutputCallback(new CallbackNQOutputStream(System.out));
		c.setLinkSelectionCallback(new LinkFilterDefault(eh));
		c.setFetchFilter(new FetchFilterRdfXml(eh));
		
		c.evaluate(seeds, rounds, threads);

		for (Throwable t : eh.getErrors()) {
			System.err.println(t.getMessage());
		}

		long time1 = System.currentTimeMillis();

		System.err.println("time elapsed " + (time1-time) + " ms");
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
