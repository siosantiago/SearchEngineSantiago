package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;


public class WebCrawler {

	
	/** Logger used for this class. */
	private static final Logger log = LogManager.getLogger();

	private final int threads;
	private final int max;
	private final HashMap<String, String> urlProcessed;
	
	public WebCrawler(int threads, int  max) {
		this.threads = threads;
		this.max = max;
		urlProcessed = new HashMap<>();
	}
	

	public void processURLS(String html) throws MalformedURLException {
		WorkQueue workQueue = new WorkQueue(threads);
		try {
			Queue<URL> urlsHtml = new LinkedList<>();
			processURLS(html, workQueue, urlsHtml, 1);
			
		}finally {
			workQueue.join();
		}
	}
	
	public void processHtml(ThreadSafeInvertedIndex invertedIndex) {
		WorkQueue workQueue = new WorkQueue(threads);
		try {
			processHtml(invertedIndex, workQueue);
		}finally {
			workQueue.join();
		}
	}
	
	public void processURLS(String html, WorkQueue workQueue, Queue<URL> urlsHtml, int current) throws MalformedURLException {

		URL htmlURL = new URL(html);
		String htmlDownload = HtmlFetcher.fetch(htmlURL, 4);
		
		if(htmlDownload != null) {
			log.fatal(" Good " + htmlURL);
			htmlDownload =  HtmlCleaner.stripBlockElements(htmlDownload);
			htmlDownload = 	HtmlCleaner.stripComments(htmlDownload);
			if(current < max) {
				Queue<URL> temp = new LinkedList<>();
				LinkFinder.findUrls(htmlURL, htmlDownload, temp);
				synchronized(urlsHtml) {
					urlsHtml.addAll(temp);
					urlProcessed.put(htmlURL.toString(), htmlDownload);
					while(urlProcessed.size() < max && !urlsHtml.isEmpty()) {
						String newBase = urlsHtml.poll().toString();
			
						if(!urlProcessed.containsKey(newBase)) {
			
							urlProcessed.put(newBase, null);
			
							workQueue.execute(new Task(newBase, workQueue, urlsHtml, urlProcessed.size()));
						}
					}
				}
			}else {
				synchronized(urlProcessed) {
					urlProcessed.put(htmlURL.toString(), htmlDownload);
				}
			}
		}
	}
	
	
	public void processHtml(ThreadSafeInvertedIndex invertedIndex, WorkQueue workQueue) {

		for(var entry: urlProcessed.entrySet()) {
			log.fatal("Ented" );
			if(entry.getValue() != null) {
				workQueue.execute(new Task3(invertedIndex, entry.getKey(),entry.getValue()));
				log.fatal("Ented" + invertedIndex.toString());
			}
			
		}
		
	}
	
	/**
	 * The task for the work queue
	 * @author santiagojaramillo
	 *
	 */
	private class Task implements Runnable {
		
		/**The html to read from*/
		private final String html;
		
		/**The work queue to add the task o*/
		private final WorkQueue workQueue;
		
		/**Where all the htmls are located*/
		private final Queue<URL> nextHtmls;
		
		
		/**The amount of htmls read*/
		private final int count;
		
		public Task(String html, WorkQueue workQueue, Queue<URL> nextHtmls, int count) {
			this.html = html;
			this.workQueue = workQueue;
			this.nextHtmls = nextHtmls;
			this.count = count;
		}

		@Override
		public void run() {
			log.debug("Got into a run");
			try {
				processURLS(html, workQueue, nextHtmls, count);
			} catch (MalformedURLException e) {
				System.out.print("Could create the web crawler");
			}

		}
	}
	
	private class Task3 implements Runnable {
		



		/**The inverted to write to*/
		private final ThreadSafeInvertedIndex invertedIndex;
		
		private final String location;
		
		private final String lines;
		
		public Task3(ThreadSafeInvertedIndex invertedIndex, String location, String lines) {
			this.invertedIndex = invertedIndex;
			this.location = location;
			this.lines = lines;
		}
		
		

		@Override
		public void run() {
			InvertedIndex localIndex = new InvertedIndex();
			int count = 1;
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			String htmlUnprocessed = lines;
			htmlUnprocessed = HtmlCleaner.stripTags(htmlUnprocessed);
			htmlUnprocessed = HtmlCleaner.stripEntities(htmlUnprocessed);
			String[] processedWords = WordCleaner.parse(htmlUnprocessed);
			log.fatal("Key" + location +" All process words" + Arrays.toString(processedWords));
			for(String word: processedWords) {
				String stemWord = stemmer.stem(word).toString();
				localIndex.addPosition(stemWord, location, count++);
			}
			
			invertedIndex.addAll(localIndex);
			log.fatal(invertedIndex.toString());
		}
	}
	
	
}



