package edu.usfca.cs272;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;


/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Santiago Jaramillo Franzoni
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver {
	
	/** Static variable to let be the basic implementation */
	public static final String DEFAULTINDEX = "index.json";

	/** Static variable to let be the result implementation */
	public static final String DEFAULTRESULT = "results.json";
	
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args all the different flags needed for the program given by the user.
	 **/
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		
		InvertedIndex invertedIndex = null; 
		ThreadSafeInvertedIndex safeIndex = null;
		
		ArgumentParser argumentParser = new ArgumentParser(args);
		WebCrawler webCrawler = null;
		
		QueryHandlerInterface queryHandler = null;
		
		boolean multiThread = false;
		int numThreads = 1;
		
		if(argumentParser.hasFlag("-html")) {
			String seed = argumentParser.getString("-html");
			
			multiThread = true;
			numThreads = argumentParser.getInteger("-threads", 5); 
			safeIndex = new ThreadSafeInvertedIndex();
			invertedIndex = safeIndex;
			queryHandler = new MultithreadedQueryHandler(safeIndex, numThreads);
			int max = 1;
			if(argumentParser.hasFlag("-max")) {
				max = argumentParser.getInteger("-max",1);
			}
			webCrawler = new WebCrawler(numThreads, max);
			try {
				webCrawler.processURLS(seed);
				webCrawler.processHtml(safeIndex);
			} catch (MalformedURLException e) {
				System.out.println("Was not able to get the web crawler working" +e);
			}
			
		}else if(argumentParser.hasFlag("-threads")) {
			multiThread = true;
			numThreads = argumentParser.getInteger("-threads", 5);
			if(numThreads < 1) {
				numThreads = 5;
			}
			 
			safeIndex = new ThreadSafeInvertedIndex();
			invertedIndex = safeIndex;
			queryHandler = new MultithreadedQueryHandler(safeIndex, numThreads);
			
		}else {
			invertedIndex = new InvertedIndex(); 
			queryHandler = new QueryHandler(invertedIndex);
		}
		
		if (argumentParser.hasFlag("-text")) {
			Path input = argumentParser.getPath("-text");
			try {
				if(multiThread) {
					MultithreadedInvertedIndexBuilder.build(input, safeIndex, numThreads);
				}
				else {
					InvertedIndexBuilder.build(input, invertedIndex);
				}
			}
			catch (IOException e) {
				System.out.println("Unable to build the inverted index from path: " + input);
			}catch (NullPointerException e) {
				System.out.println("Unable to build the inverted index from path: null");
			}
		}
		
		if(argumentParser.hasFlag("-query")) {
			try {
				Path input = argumentParser.getPath("-query");
				queryHandler.parseQueryFile(input, argumentParser.hasFlag("-exact"));	
			} catch (IOException e) {
				System.out.println("Unable to query from file ");
			} catch (NullPointerException e) {
				System.out.println("No query retrivable");
			}
			
		}
		if (argumentParser.hasFlag("-index")) {
			Path output = argumentParser.getPath("-index", Path.of(DEFAULTINDEX));
			try {
				invertedIndex.toJSON(output);
			}
			catch (IOException e) {
				System.out.println("Unable to add to index");
			}
		}
		
		if(argumentParser.hasFlag("-counts")) {
			Path output = argumentParser.getPath("-counts");
			try {
				invertedIndex.countToJSON(output);
			} catch (IOException e) {
				System.out.println("Unable to count from -counts");
			}
		}
		
		if(argumentParser.hasFlag("-results")) { 
			Path output = argumentParser.getPath("-results", Path.of("results.json"));
			try {
				queryHandler.toJSON(output);
			} catch (IOException e) {
				System.out.println("Could not write to file" + output);
			}
		}


		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}	
	
}
