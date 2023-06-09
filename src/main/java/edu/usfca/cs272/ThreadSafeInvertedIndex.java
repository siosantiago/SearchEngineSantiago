package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The same as the inverted index but with a read write lock for safe multithreading
 * @author santiagojaramillo
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	
	/**the lock to multi thread safely with*/
	private final ReadWriteLock lock;
	
	/**Logger for errors*/
	private static final Logger log = LogManager.getLogger();

	/**
	 * Constructor with read lock 
	 */
	public ThreadSafeInvertedIndex() {
		super();
		this.lock = new ReadWriteLock();
	}
	
	@Override
	public int numWords() {
		lock.read().lock();
		try {
			return super.numWords();
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public int numLocations(String word) {
		lock.read().lock();
		try {
			return super.numLocations(word);
		}finally {
			lock.read().unlock();
		}
		
	}

	@Override
	public int numPositions(String word, String location) {
		lock.read().lock();
		try {
			return super.numPositions(word, location);
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public boolean containsWord(String word) {
		lock.read().lock();
		try {
			return super.containsWord(word);
		}finally {
			lock.read().unlock();
		}		
	}

	@Override
	public boolean containsLocation(String word, String location) {
		lock.read().lock();
		try {
			return super.containsLocation(word, location);
		}finally {
			lock.read().unlock();
		}		
	}

	@Override
	public boolean containsPosition(String word, String location, int position) {
		lock.read().lock();
		try {
			return super.containsPosition(word, location, position);
		}finally {
			lock.read().unlock();
		}		
	}

	@Override
	public Set<String> getWords() {
		lock.read().lock();
		try {
			return super.getWords();
		}finally {
			lock.read().unlock();
		}	
	}

	@Override
	public Set<String> getLocations(String word) {
		lock.read().lock();
		try {
			return super.getLocations(word);
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String word, String location) {
		lock.read().lock();
		try {		
			return super.getPositions(word, location);
		}finally {
			lock.read().unlock();
		}		
	}

	@Override
	public boolean addPosition(String word, String location, int count) {
		log.debug("Locking add position");
		lock.write().lock();
		try {
			return super.addPosition(word, location, count);
		}finally {
			lock.write().unlock();
			log.debug("Unlocking add position");
		}
		
	}

	@Override
	public String toString() {
		lock.read().lock();
		try {
			return super.toString();
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public void toJSON(Path path) throws IOException {
		lock.read().lock();
		try {
			super.toJSON(path);
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public void countToJSON(Path output) throws IOException {
		lock.read().lock();
		try {
			super.countToJSON(output);
		}finally {
			lock.read().unlock();
		}		
	}
	
	@Override
	public void addAll(InvertedIndex invertedIndex) {
		lock.write().lock();
		try {
			super.addAll(invertedIndex);
		}finally {
			lock.write().unlock();
		}
	}

	@Override
	public List<PageScore> search(Set<String> queries, boolean exact) {
		lock.read().lock();
		try {
			return super.search(queries, exact);
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public int getCount(String location) {
		lock.read().lock();
		try {
			return super.getCount(location);
		}finally {
			lock.read().unlock();
		}
	}

	@Override
	public boolean containsLocation(String location) {
		lock.read().lock();
		try {
			return super.containsLocation(location);
		}finally {
			lock.read().unlock();
		}	
	}

	@Override
	public Set<String> getLocationsFilesCounter() {
		lock.read().lock();
		try {
			return super.getLocationsFilesCounter();	
		}finally {
			lock.read().unlock();
		}
	}
}
