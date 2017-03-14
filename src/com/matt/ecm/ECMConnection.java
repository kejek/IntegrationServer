package com.matt.ecm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ECMConnection {
	private static final String USER_AGENT_HEADER = null;
	private static final String USER_AGENT = null;
	String userNameHeader = null;
	String passwordHeader = null;
	String ecmUsername = null;
	String ecmPassword = null;
	String ecmURL = null;
	String sessionHash = null;

	/**
	 * @author Matt
	 * @throws Exception
	 * Opens connection with ECM Integration Servers
	 */
	public void openConnection() throws Exception {
		
		
		String url = ecmURL + "v1/connection";

		try {
			initAppConfigValues();
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
			con.setRequestProperty(userNameHeader, ecmUsername);
			con.setRequestProperty(passwordHeader, ecmPassword);
			con.setRequestProperty("Content-Type", "application/json");

			boolean redirect = false;
			int responseCode = con.getResponseCode();

			// Check for a redirect response, usually a 3xx response code
			if (responseCode != HttpURLConnection.HTTP_OK) {
				if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
						|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
						|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}

			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = con.getHeaderField("Location");
				URL newObj = new URL(newUrl);
				HttpURLConnection newCon = (HttpURLConnection) newObj.openConnection();

				// add request header
				newCon.setRequestMethod("GET");
				newCon.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
				newCon.setRequestProperty(userNameHeader, ecmUsername);
				newCon.setRequestProperty(passwordHeader, ecmPassword);
				newCon.setRequestProperty("Content-Type", "application/json");

				url = newUrl;
				con = newCon;
				ecmURL = url.replace("v1/connection", "");

			}
			responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			parseSessionInformation(con);
			
		} catch (Exception e) {
			//Throw exception, since connection does not open
			throw e;
		}
}

	private void initAppConfigValues() {
		// TODO: Get Values from Database
		userNameHeader = "X-IntegrationServer-Username";
		passwordHeader = "X-IntegrationServer-Password";
		ecmUsername = "<ecmUsername>";
		ecmPassword = "<ecmPassword>";
		ecmURL = "<ecmUrl>";
		
		
	}

	private void parseSessionInformation(HttpURLConnection con) {
		// TODO: Parse session information for Cookie changes or SessionHash changes
		sessionHash = con.getHeaderField("X-IntegrationServer-SessionHash");
	}
}
