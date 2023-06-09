package edu.usfca.cs272;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class HtmlFetcher {
	
	/** Logger used for this class. */
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and
	 * the first value of that header starts with the value "text/html"
	 * (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		if(headers.containsKey("Content-Type")) {
			if(headers.get("Content-Type").get(0).startsWith("text/html")) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		if(headers.containsKey(null)) {
			String status = headers.get(null).get(0);
			try {
				int state = Integer.parseInt(status.substring(9, 12));
				return state;
			}catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Returns {@code true} if and only if the HTTP status code is between 300 and
	 * 399 (inclusive) and there is a "Location" header with at least one value.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isRedirect(Map<String, List<String>> headers) {
		int status = getStatusCode(headers);
		if(status >= 300 && status < 400) {
			return true;
		}
		return false;
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect
	 * if the number of redirects is greater than 0. Otherwise, returns
	 * {@code null}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 *
	 * @see #isHtml(Map)
	 * @see #isRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		String html = null;

		try (
				Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input);
		) {
			HttpsFetcher.printGetRequest(request, url);
			Map<String, List<String>> results = HttpsFetcher.getHeaderFields(response);
			log.debug(results.toString());
			if(isHtml(results)) {
				log.debug("Got into isHtml");
				int status = getStatusCode(results);
				if(status == 200) {
					log.debug("Got into 200");
					results = HttpsFetcher.fetchUrl(url);
					if(results.containsKey("Content")) {
						StringBuffer strB = new StringBuffer();
						for(String content: results.get("Content")) {
							strB.append(content + "\n");
						}
						html = strB.toString();
					}
				}else if (isRedirect(results) && redirects > 0) {
					log.debug("Got into redirect" + getStatusCode(results));
					//results = HttpsFetcher.fetchUrl(url);
					if(results.containsKey("Location")) {
						
						return fetch(results.get("Location").get(0), redirects - 1);
					}else if(results.containsKey("Content")) {
						List<URL> urls = new ArrayList<>();
						LinkFinder.findUrls(url, results.get("Content").toString(), urls);
						log.debug("Urls: - " + urls.toString());
						if(urls.size() > 0) {
							log.debug("urls.get(0)" + urls.get(0));
							return fetch(urls.get(0), redirects - 1);
						}
					}
						
				}
			}
		}
		catch (IOException e) {
			html = null;
		}

		return html;
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 * @throws IOException if unable to process url
	 */
	public static void main(String[] args) throws IOException {
		String link = "https://usf-cs272-fall2022.github.io/project-web/input/birds/falcon.html";
		System.out.println(link);
		System.out.println(fetch(link));
	}
}
