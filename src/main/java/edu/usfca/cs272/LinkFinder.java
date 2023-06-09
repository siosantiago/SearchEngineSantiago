package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds HTTP(S) URLs from the anchor tags within HTML code.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class LinkFinder {
	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute
	 * of the anchor tags in the provided HTML. The URLs will be converted to
	 * absolute using the base URL and normalized (removing fragments and encoding
	 * special characters as necessary).
	 *
	 * Any URLs that are unable to be properly parsed (throwing an
	 * {@link MalformedURLException}) or that do not have the HTTP/S protocol will
	 * not be included.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @param urls the data structure to store found HTTP(S) URLs
	 *
	 * @see Pattern#compile(String)
	 * @see Matcher#find()
	 * @see Matcher#group(int)
	 * @see #normalize(URL)
	 * @see #isHttp(URL)
	 */
	public static void findUrls(URL base, String html, Collection<URL> urls) {
		String regex = "(?i)<a.*?>";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);
		while(matcher.find()) {
			String checkURL = matcher.group();
			int indexHref = checkURL.toLowerCase().indexOf("href");
			if(indexHref == -1) {
				continue;
			}
			
			int start = checkURL.indexOf("\"", indexHref);
			int end = checkURL.indexOf("\"", start+1);
			if(start != end && (start > 0 && end > 1)) {
				String urlNotAbs = checkURL.substring(start+1, end);
				try {
					if(urlNotAbs.startsWith("http")) {
						URL url = new URL(urlNotAbs);
						url = normalize(url);
						urls.add(url);
					}else {
						URL absolute = new URL(base, urlNotAbs);
						absolute = normalize(absolute);
						urls.add(absolute);
					}
				} catch (MalformedURLException e) {
					System.out.println(e);
				} catch (URISyntaxException e) {
					System.out.println(e);
				}
			}
		}
	}

	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute
	 * of the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) URLs in the order they were found
	 *
	 * @see #findUrls(URL, String, Collection)
	 */
	public static ArrayList<URL> listUrls(URL base, String html) {
		ArrayList<URL> urls = new ArrayList<URL>();
		findUrls(base, html, urls);
		return urls;
	}

	/**
	 * Returns a set of all the unique valid HTTP(S) URLs found in the HREF
	 * attribute of the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) URLs in the order they were found
	 *
	 * @see #findUrls(URL, String, Collection)
	 */
	public static HashSet<URL> uniqueUrls(URL base, String html) {
		HashSet<URL> urls = new HashSet<URL>();
		findUrls(base, html, urls);
		return urls;
	}

	/**
	 * Removes the fragment component of a URL (if present), and properly encodes
	 * the query string (if necessary).
	 *
	 * @param url the URL to normalize
	 * @return normalized URL
	 * @throws URISyntaxException if unable to craft new URI
	 * @throws MalformedURLException if unable to craft new URL
	 */
	public static URL normalize(URL url) throws MalformedURLException, URISyntaxException {
		return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
				url.getPort(), url.getPath(), url.getQuery(), null).toURL();
	}

	/**
	 * Determines whether the URL provided uses the HTTP or HTTPS protocol.
	 *
	 * @param url the URL to check
	 * @return true if the URL uses the HTTP or HTTPS protocol
	 */
	public static boolean isHttp(URL url) {
		return url.getProtocol().matches("(?i)https?");
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 * @throws Exception if any issues occur
	 */
	public static void main(String[] args) throws Exception {
		// this demonstrates cleaning
		URL valid = new URL("https://docs.python.org/3/library/functions.html?highlight=string#format");
		System.out.println(" Link: " + valid);
		System.out.println("Clean: " + normalize(valid));
		System.out.println();

		// this demonstrates encoding
		URL space = new URL("https://www.google.com/search?q=hello world");
		System.out.println(" Link: " + space);
		System.out.println("Clean: " + normalize(space));
		System.out.println();

		// this demonstrates a non-HTTP URL
		URL email = new URL("mailto:username@example.edu");
		System.out.println(email);
		System.out.println("HTTP? " + isHttp(email));

		// this throws an exception
		URL invalid = new URL("javascript:alert('Hello!');");
		System.out.println(invalid);
	}
}
