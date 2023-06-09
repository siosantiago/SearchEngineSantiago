package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * same as the builder for index but multithreaded
 * @author santiagojaramillo
 *
 */
public class MultithreadedInvertedIndexBuilder{
	
	/** Logger used for this class. */
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Method that creates the inverted index data structure by adding task to a newly made workQueue
	 * this task are basically finding the words in a file and adding them to the inverted index
	 * @param input the path to read all files existing in this trajectory or file.
	 * @param invertedIndex the invertedIndex being build.
	 * @param threads the amount of threads the worker needs.
	 * @throws IOException reading a file can throw exceptions.
	 */
	public static void build(Path input, ThreadSafeInvertedIndex invertedIndex, int threads) throws IOException {
		WorkQueue workQueue = new WorkQueue(threads);
		try {
			if (Files.isDirectory(input)) {
				HashSet<Path> hashSet = DirectoryTraverser.getAllTextFiles(input);
				for(Path path: hashSet) {
					workQueue.execute(new Task(path, invertedIndex));
				}
			}
			else {
				workQueue.execute(new Task(input, invertedIndex));
			}
		}finally {
			workQueue.join();
		}
	}
	
	/**
	 * The task for the work queue
	 * @author Santiago Jaramillo
	 *
	 */
	private static class Task implements Runnable {

		/**The file to be reading from*/
		private final Path file; 
		/**The inverted to write to*/
		private final ThreadSafeInvertedIndex invertedIndex;
		
		/**
		 * The task at hand
		 * @param file to write to
		 * @param invertedIndex invert index to save
		 */
		public Task(Path file, ThreadSafeInvertedIndex invertedIndex) {
			this.file = file;
			this.invertedIndex = invertedIndex;
		}
		
		@Override
		public void run() {
			log.debug("Got into a run");
			InvertedIndex localIndex = new InvertedIndex();
			try {
				InvertedIndexBuilder.addFile(localIndex, file);
			} catch (IOException e) {
				System.out.println("error found" + e);
			}
			log.debug("About to start into add all");
			invertedIndex.addAll(localIndex);
			log.debug("Finished into a run");
		}
	}
}
