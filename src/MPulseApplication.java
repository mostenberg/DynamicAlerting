import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

/**
 * Each mPulse application object represents an interface to a particular
 * application in the mPulse system. You can have many mPulse applications
 * defined.
 *
 * 
 */
public class MPulseApplication {

	private static int applicationCount;
	private static MPulseApplication[] mPulseApplications;

	/**
	 * @return the number of Applications that have been defined so far
	 */
	public static int getApplicationCount() {
		return mPulseApplications.length;
	}

	/**
	 * @return the list of mPulse applications names which have already been defined
	 */
	public static ArrayList<String> getApplicationNames() {
		ArrayList<String> applicationNames = new ArrayList<String>();

		for (int i = 0; i < getApplicationCount(); i++) {
			applicationNames.add(mPulseApplications[i].name);
		}
		return applicationNames;
	}

	/**
	 *
	 */
	protected String name;
	private String username = null; // "macy-jenkins";
	private String password = null; // "jenkins";
	private String urlString = "https://mpulse.soasta.com/concerto/services/rest/RepositoryService/v1/Tokens";
	private String token = null;
	private final String USER_AGENT = "Mozilla/5.0";
	private String response = null;
	private String reqbody = null;
	private String apiToken = null;
	private String apiKey = null;
	String tenantName = null;
	// https://mpulse.soasta.com/concerto/mpulse/api/v2/XXXXX-YYYYY-ZZZZZ-XXXXX-YYYYY/summary?date=2013-05-10
	final String urlStringForQueries = "https://mpulse.soasta.com/concerto/mpulse/api/v2";
	// private String reqbody = "{\"userName\":\"macy-jenkins\",
	// \"password\":\"jenkins\"}";

	/**
	 *
	 * @param name
	 *            A friendly name for this particular mPulse Application
	 * @param username
	 *            A username with data permissions to this mPulse Application
	 * @param password
	 *            The password for the username with data permissions to this mPulse
	 *            application
	 * @param apiToken
	 *            The API key for this particular mPulse Application
	 */
	public MPulseApplication(String name, String username, String password, String apiToken, String tenantName, String apiKey) {
		this.name = name;
		this.username = username;
		this.password = password;
		this.apiToken = apiToken;
		this.apiKey = apiKey;
		this.tenantName=tenantName;
		if ((apiToken.length()>0) && (tenantName.length()>0)){ reqbody="{\"apiToken\":\"" + apiToken + "\",\"tenant\":\""+tenantName+"\"}";}
		else if (apiToken.length()>0) 						 { reqbody="{\"apiToken\":\"" + apiToken + "\"}"; }
		else if (username.length()>0){ reqbody = "{\"userName\": \"" + username + "\",\"password\": \"" + password + "\"}";
		}
		//if (tenantName.length()>0) {reqbody="{\"tenant\":\"" + tenantName + "\"}";}
	}

	public MPulseApplication(String name, String apiToken, String tenantName, String apiKey) {
		this.name = name;
		this.apiToken = apiToken;
		this.apiKey = apiKey;
		this.tenantName=tenantName;
		if ((apiToken.length()>0) && (tenantName.length()>0)){ reqbody="{\"apiToken\":\"" + apiToken + "\",\"tenant\":\""+tenantName+"\"}";}
		else if (apiToken.length()>0) 						 { reqbody="{\"apiToken\":\"" + apiToken + "\"}"; }
		else if (username.length()>0){ reqbody = "{\"userName\": \"" + username + "\",\"password\": \"" + password + "\"}";
		}
		//if (tenantName.length()>0) {reqbody="{\"tenant\":\"" + tenantName + "\"}";}
	}
	/**
	 * Calls he mPulse API to create the token that will be used for all future
	 * requests.
	 * 
	 * @return The token value (after setting the token for the current
	 *         MPulseApplication)
	 */
	public String requestmPulseToken() {
		String inputLine = null;
		try {
			URL myurl = new URL(urlString);
			HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
			con.setRequestMethod("PUT"); // type: POST, PUT, DELETE, GET
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("User-Agent", USER_AGENT);
			// con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setConnectTimeout(60000); // 60 secs
			con.setReadTimeout(60000); // 60 secs
			// con.setRequestProperty("Content-Type:", "application/json");
			System.out.println("Request body is " + reqbody);
			if (reqbody != null) {
				con.setDoInput(true);
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(reqbody);
				out.flush();
				out.close();
			}
			InputStream ins = con.getInputStream();
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);

			while ((inputLine = in.readLine()) != null) {
				response = inputLine;
			}
			in.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
		}
		System.out.println("Response is"+response);
		JSONObject jobj = new JSONObject(response);
		token = jobj.getString("token");
		System.out.println("Token is: " + token);

		return token;
	}

	/**
	 * Calls the mPulse API to retrieve information.
	 * 
	 * @param queryString
	 *            The entire queryString for a request to the mPulse API.
	 *            Essentially this everything to the right of the question mark in
	 *            the following sample mPulse request:\n\t
	 * 
	 *            <pre>
	 * {@code https://mpulse.soasta.com/concerto/mpulse/api/v2/XXXXX-YYYYY-ZZZZZ-XXXXX-YYYYY/summary?date=2013-05-10}
	 * 			</pre>
	 * 
	 * @return the response to the API request.
	 */
	public String mPulseQuery(String queryString) {
		String inputLine = null;
		try {
			// https://mpulse.soasta.com/concerto/mpulse/api/v2/XXXXX-YYYYY-ZZZZZ-XXXXX-YYYYY/summary?date=2013-05-10"
			urlString = String.format("%s/%s/%s", this.urlStringForQueries, this.apiKey, queryString);
			//urlString = String.format("%s/%s/%s&tenant=%s", this.urlStringForQueries, this.apiKey,queryString,tenantName);
			urlString = urlString.replace(" ", "%20");
			URL myurl = new URL(urlString);
			HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
			con.setRequestMethod("GET"); // type: POST, PUT, DELETE, GET
			con.setDoOutput(false);
			con.setDoInput(true);
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Authentication", token);
			con.setConnectTimeout(60000); // 60 secs
			con.setReadTimeout(60000); // 60 secs
			InputStream ins = con.getInputStream();
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);

			while ((inputLine = in.readLine()) != null) {
				response = inputLine;
			}
			in.close();

		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
		}
		return response;
	}

}
