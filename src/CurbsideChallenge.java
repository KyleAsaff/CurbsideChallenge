/*************************************************
 *
 *  CurbsideChallenge.java
 *  Author: Kyle Asaff
 *  Description: Solves the Curbside Challenge
 *
 *************************************************/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;
import org.json.*;

public class CurbsideChallenge {
	
	// Default USER_AGENT
	private final String USER_AGENT = "Mozilla/5.0";
	
    /**
     * Prints the final "decoded" message after finding all secrets
     * 
     * @throws IOException
     */
	public static void main(String[] args) throws Exception {

		// Create new HTTP
		CurbsideChallenge http = new CurbsideChallenge();

		// Initialize program and print the result of all secrets
		String message = http.init(http.getSession());
		System.out.println(message);

	}

	/**
	 * Gets a new session ID for the HTTP header.
	 * 
	 * @return the session ID as a string
	 * @throws IOException
	 */
	private String getSession() throws Exception {

		String url = "http://challenge.shopcurbside.com/get-session";

		//Build the HTTP Client Request
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// Add USER_AGENT to header
		request.addHeader("User-Agent", USER_AGENT);

		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		// Store result from GET request as a String
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();

	}

	/**
	 * Initialize the program by starting with the IDs at depth 1 and recursively 
	 * call all IDs found.
	 * 
	 * @param session the initial session header for the HTTP GET request. 
	 * @return the final message of the puzzle as a string
	 * 		   after appending all secrets found.
	 * @throws IOException
	 */
	private String init(String session) throws Exception {

		String url = "http://challenge.shopcurbside.com/start";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// Add Session ID to request header
		request.addHeader("Session", session);

		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		// Store result from GET request as a String
		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		String res = result.toString();

		// Create ArrayList for secrets
		ArrayList<String> list = new ArrayList<String>();
		JSONObject obj = new JSONObject(res);
		JSONArray arr = obj.getJSONArray("next");

		// Find all secrets
		for (int i = 0; i < arr.length(); i++) {
			recursiveID(arr.getString(i), list);
		}

		StringBuilder str = new StringBuilder();

		// Append all secrets to str
		for (String i : list) {
			str.append(i);
		}

		// Return final message
		return str.toString();
	}

	/**
	 * Recursive call to find all secrets. When a secret is found it is added
	 * to the ArrayList.
	 * 
	 * @param id the ID for the recursive call
	 * @param list the ArrayList to hold all secrets found in order.
	 * @return the final message of the puzzle as a string
	 * 		   after appending all secrets.
	 * @throws IOException
	 */
	private void recursiveID(String id, ArrayList<String> list)
			throws Exception {

		// Add ID to URL
		String url = "http://challenge.shopcurbside.com/" + id;

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// Add Session ID to request header
		request.addHeader("Session", getSession());

		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		// Store result from GET request as a String
		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		String res = result.toString().toLowerCase();

		// Convert GET request String to a JSONObject
		JSONObject obj = new JSONObject(res);

		// If JSON Object has secret, add to list
		if (obj.has("secret")) {
			list.add(obj.getString("secret"));
		// If there is a "next" key, loop through all IDs
		} else if (obj.has("next")) {
			JSONArray arr = obj.optJSONArray("next");
			if (arr == null) {
				recursiveID(obj.getString("next"), list);
			} else {
				for (int i = 0; i < arr.length(); i++) {
					recursiveID(arr.getString(i), list);
				}
			}
		}
	}
}
