
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * This is the Main class for the DynamicAlerting.jar package. It parses the
 * command line arguments to get the appropriate data ranges The class
 * MPulseJenkins has the Main method and is the entry point for the program.
 * MPulseJenkins.Main will call MPulseJenkins.parseArgumentsTosetNameUsernameAPIandPWD function. 
 * This will provide the account credentials to create an object of type MPulseApplication
 * (which holds login info). 
 * MPulseJenkins.Main will then use MPulseApplication to get a request token. 
 * MPulseJenkins.Main will then create a MPulseDataSet called mpds and embedding the mPulse Application and credentials. 
 * mpds is basically the object that holds the two data sets (baseline and trial) and compares them. 
 * MPulseJenkins.Main will then parse the rest of the arguments for timeline etc. 
 * As each argument is parsed, the appropriate value in mpds is set. 
 * Then, calling mpds.setBaseline() will have mpds create the query string and initiate the call to mPulse for the baseline data. 
 * Then, calling mpds.setTestData() will have mpds create the query string and initiate the
 * call to mPulse for the test data. 
 * Now that mpds has the data there are 3
 * functions to output comperisons to CSV, XML and HTML files:
 *  mpds.getHTMLSummary(); //Gets the HTML summary of the comparison
 *  mpds.getXMLSummary();  //Gets the XML summary of the comparison
 *  mpds.writeCSVSummaries(); //Writes the CSV files used by the Plot plugin to plot the data.
 *  
 * @author mikeostenberg
 */
public class MPulseJenkins {

	static String name = null;
	static String username = null;
	static String password = "";
	static String apiKey = "";
	static String apiToken = "";
	static Double threshold = 1.2;
	static String HTMLSummary = ""; // -Hack- moved it up to static so that I could reference from Junit
    static String tenantName="";

	/**
	 *
	 */

	public MPulseJenkins() {
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Arguments Summary:");
		for (int i=0;i<args.length;i++) {System.out.println("\tArg # "+i+" is "+args[i]);}
		System.out.println("Ending");

		System.out.println("mPulse/Jenkins Utility : \n"
				+ "  Version 1.3  Last Modified 2/24/2018 by Mike Ostenberg mostenbe@akamai.com .Updated to automatically detect and use local  timezone \n"
				+ "  Version 1.4  Last Modified 3/16/2018 by Mike Ostenberg mostenbe@akamai.com .Updated to better handle scientific notation in returned results\n"
				+ "  Version 1.5  Last Modified 3/31/2018 by Mike Ostenberg mostenbe@akamai.com .Updated to allow setting threshold based on stdev\n"
				+ "  This utility will allow you to execute a jar file from the command line, and automatically compare recent mPulse performance to a time window of your choosing, then PASS/FAIL\n"
				+ "  based on thresholds that you set. Execute by running java -jar DynamicAlerting.jar [args]\n\n"
				+ "Command line arguments:\n"
				+ "\tbaselineStartDate=<dateFormat> - The start date for the period to derive your baseline from\n"
				+ "\tbaselineEndDate=<dateFormat> -   The end  date for the period to derive your baseline from.\n"
				+ "\tbaselineDomain=<domain> - The domain to measure for your baseline. Note: This requires you to setup a custom dimension called 'subDomain' in your application.\n"
				+ "\tbaselineMultiplier=<float> - The multiplier above baseline time  (e.g. If baselineMultiplier=1.2  and HomePage was 3.2 for baseline, then threshold will be 3.84)\n"
				+ "\tbaselineMultiplier=<float>sd - Placind 'sd' after the baseline multiplier indicates you want baseline to be based on # of standard deviations above\n"
				+ "\t                               the basedline so baselineMultiplier=1.5sd would indicate that threshold will be 1.5 standard deviations above baseline\n"
				+ "\ttestStartDate=<dateFormat> - The date for your measurements to start (REQUIRED). Usually this is -5minutes to aggregate from 5 minutes ago\n"
				+ "\ttestEndDate=<dateFormat> - The end dateTime for your measurement window (usually 'now') (REQUIRED) Usuaully this is 'now' to measure up until current time.\n"
				+ "\ttestDomain=<domain> - The name of the domain to measure for your test measurements.Note: This requires you to setup custom dimension called 'subDomain' in your application.\n"
				+ "\tglobalThreshold=<float> - A global threshold (in milliseconds) to apply to any page groups where there is no dynamic value or manual value set\n"
				+ "\tusername=<string> - The username to use when logging in\n"
				+ "\tpassword=<string> - The password to use when requesting mPulse data\n"
				+ "\tapiToken=<string> - The apiToken of your userID (can be used instead of username/password). Found in mPulse 'My Settings'.	\n"
				+ "\tapiKey=<string> - The apiKey of the app to request data for	\n"
				+ "\ttenantName=<string> - The tenant where the app is located (only needed on multi-tenant logins	\n"
				+ "\tallowedPercentOfPageGroupsFailing=<FLOAT> - The percentage of pageGroups that can fail, and still register this job as 'Stable'  (For instance 50.0 would allow half of pageGroups to exceed threshold)\n"
				+ "\tminMeasurements=<INT> - The minimum number of measurements in the test data. If there are less measurements, we will perform a check on that pageGroup.\n"
				+ "\tthreshold_<SOMENAME>=<FLOAT> - A specific hardcoded threshold value for a particular pageGroup (Example: threshold_HomePage=6.32 )\n"
				+ "\tqueryModification=<STRING> - By default we use the mPulse API from here: http://docs.soasta.com/query-api/#page-groups-query-and-response to query on page group with no filter. \n"
				+ "\t\t   You can modify that behavior to replace 'pagegroup?' with the string of your choice. Some common options might be: \n"
				+ "\t\t   queryModification=browser?   - this will give breakdown by browsers.\n"
				+ "\t\t   queryModification=page-groups?country=US&   - this will filter to just US as the country.\n"
				+ "\t\t   queryModification=browsers?country=US&page-group=shop-category&    - This will show response times of all browsers from the US for the pageGroup of 'shop-category'\n"
				+ "\t\n" + "\tDate Formats accepted:  \n"
				+ "\t\tMM/DD/YYYY - A specific date. If no time is specified, assumed midnight at the START of that date\n"
				+ "\t\thh:mmAM-MM/DD/YY - A date and time. For example:  baselineStartDate=3:00PM-08/01/2015\n"
				+ "\t\t-Xdays - A relative time from right now. For instance, -3days sets time at midnight 3 days ago\n"
				+ "\t\ttoday - Today at 0:00AM  (Note: This is last midnight, not the upcoming midnight)\n"
				+ "\t\t-Xhours - A relative time from right now going back 'X' hours\n"
				+ "\t\t-Xminutes - A relative time from right now going back 'X' minutes\n" + "\t\tnow - Current time."
				+ "\n\tThreshold Description:\n" + "\t\tThresholds are evaluated as follows:\n"
				+ "\t\t  If a manual threshold was set for a specific pageGroup, that value takes precedence\n"
				+ "\t\t  If a baselineMultiplier and baselineStartDate and baseLineEndDate was set (and no manual threshold) then the dynamic value will be calculated and used.\n"
				+ "\t\t  If no manual threshold or baselineMultiplier was set, then the globalThreshold value will be used.\n"
				+ "\t\t  Note: It's a good idea to always have some globalThreshold, as a 'failsafe' since there are situations where your baseline not have a value..\n"
				+ "\t\t   For example, if you have just recently created a pageGroup, your baseline won't have data to create a treshold from, and 1.2* baseline=0 sec if no baseline.\n\n"
				+ "\n");

		if (args.length == 0) {
			System.out.println(
					"\n****** Exiting since no arguments provided from command line\n");
			return;
		}
		MPulseJenkins.parseArgumentsTosetNameUsernameAPIandPWD(args);// First parse command line arguments to set the
																		// Username and apiKey
		MPulseApplication mps = new MPulseApplication(name, username, password, apiToken,tenantName,apiKey); // Create an mPulse application
																							// object which holds these
																							// credentials
		mps.requestmPulseToken(); // Get the mPulse token and store it in the mps object.
		MPulseDataSet mpds = new MPulseDataSet(mps); // Create a DataSet object related to this mPulse object (currently
														// 'empty' until we pass in the baseline and test data JSON
														// strings)
		MPulseJenkins.parseArguments(args, mpds);  // Parse the command line arguments to get query ranges etc. These are
													// stored in the mpds object, thus setting initial query ranges for
													// test and baseline.
		if (mpds.testStartDate == null || mpds.testEndDate == null) {
			System.out.println("Test start date and end date are required\n");
			return;
		}
		if (mpds.baselineStartDate == null && PageGroup.globalThreshold == 0 && PageGroup.manualThresholdCount == 0) {
			System.out.println("Must have baseline or manual threshold or global threshold\n");
			return;
		}
	
		mpds.setBaseline(); // Tells the mPulse DataSet to initiate the query setting to mPulse and create
							// baseline DataSet(Date ranges were passed in earlier)
		mpds.setTestData();

		mpds.parseJSONStringToCreatePageGroups();

		HTMLSummary = mpds.getHTMLSummary();

		System.out.println(String.format("HTML SUMMARY:\n\n\t%s\n", HTMLSummary));
		PrintWriter writer;
		try {
			writer = new PrintWriter("mPulseHTMLSummary.html", "UTF-8");
			writer.println(HTMLSummary);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String XMLSummary = mpds.getXMLSummary();
		System.out.println(String.format("XML Summary:\n\n\t%s\n", XMLSummary));
		try {
			writer = new PrintWriter("mPulseXMLSummary.xml", "UTF-8");
			writer.println(XMLSummary);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mpds.writeCSVSummaries();

	}
/**
 *  Parses the command line arguments related to time comparison and thresholds.
 *  Note: The first pass on the command line arguments was done by the metho
 * @param myArgs
 * @param mpds
 */
	static void parseArguments(String[] myArgs, MPulseDataSet mpds) {
		// System.out.println ("Number of Arguments: "+myArgs.length);

		for (String arg : myArgs) {
			String argname;
			/*
			 * patternString = ("baselineStartDate=(.+)"); Pattern p
			 * =Pattern.compile(patternString); Matcher m =p.matcher(arg); if (m.find()){
			 * System.out.println ("Found value for baselineStartDate of "+m.group(1));
			 * mps.baselineStartDate=MPulseDataSet.getDate(m.group(1));
			 * System.out.println("Set baselineDataStart to "+mps.baselineStartDate.getTime(
			 * ).toString()); }
			 */

			argname = "baselineStartDate=";
			if (arg.startsWith(argname)) {
				mpds.baselineStartDate = MPulseDataSet.getDate(arg.substring(argname.length(), arg.length()));
				System.out.println(String.format("Based on command line argument of %s , set baselineStartDate to %s",
						arg, mpds.baselineStartDate.getTime().toString()));
			}


			argname = "baselineEndDate=";
			if (arg.startsWith(argname)) {
				mpds.baselineEndDate = MPulseDataSet.getDate(arg.substring(argname.length(), arg.length()));
				System.out.println(String.format("Based on command line argument of %s , set baselineEndDate to %s",
						arg, mpds.baselineEndDate.getTime().toString()));
			}

			argname = "testStartDate=";
			if (arg.startsWith(argname)) {
				mpds.testStartDate = MPulseDataSet.getDate(arg.substring(argname.length(), arg.length()));
				System.out.println(String.format("Based on command line argument of %s , set testStartDate to %s", arg,
						mpds.testStartDate.getTime().toString()));
			}

			argname = "testEndDate=";
			if (arg.startsWith(argname)) {
				mpds.testEndDate = MPulseDataSet.getDate(arg.substring(argname.length(), arg.length()));
				System.out.println(String.format("Based on command line argument of %s , set testEndDate to %s", arg,
						mpds.testEndDate.getTime().toString()));
			}

			argname = "testDomain=";
			if (arg.startsWith(argname)) {
				mpds.testDomain = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set testDomain to %s", arg,
						mpds.testDomain));
			}

			argname = "baselineDomain=";
			if (arg.startsWith(argname)) {
				mpds.baselineDomain = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set baselineDomain to %s", arg,
						mpds.baselineDomain));
			}

			argname = "threshold_"; // Set manual threshold.. must parse out of threshold_login=6.34
			if (arg.startsWith(argname)) {
				String pageGroupName = arg.substring(10, arg.indexOf('='));
				Integer manualThresholdTime = Math
						.round(1000 * Float.valueOf(arg.substring(arg.indexOf('=') + 1, arg.length())));
				PageGroup myPGD;
				if (!mpds.pageGroups.containsKey(pageGroupName)) {
					myPGD = new PageGroup(pageGroupName);
				} // Add the pageGroup if it doesn't exist
				else {
					myPGD = mpds.pageGroups.get(pageGroupName);
				} // Get the existing PageGroup object if it exists.
				myPGD.setManualThreshold(manualThresholdTime);
				mpds.pageGroups.put(pageGroupName, myPGD);
				PageGroup.manualThresholdCount++;
				System.out.println(String.format(
						"Based on command line argument of %s , set pageGroup to %s and set threshold to %dms ", arg,
						pageGroupName, manualThresholdTime));
			}

			argname = "baselineMultiplier=";
			if (arg.startsWith(argname)) {
				String stdAdder=""; //If this is a standardDeviation
				if (arg.endsWith("sd")) {mpds.isStdDevMode=true;arg=arg.substring(0, arg.length()-2);stdAdder="stDev";}//set stdDev mode and remove the 'sd'
				mpds.baselineMultiplier = Double.parseDouble(arg.substring(argname.length(), arg.length()));
				System.out
						.println(String.format("Based on command line argument of %s , set baselineMultiplier to to %s%s",
								arg, mpds.baselineMultiplier,stdAdder));
			}

			argname = "globalThreshold=";
			if (arg.startsWith(argname)) {
				PageGroup.globalThreshold = Math
						.round(1000 * Float.parseFloat(arg.substring(argname.length(), arg.length())));
				System.out.println(String.format("Based on command line argument of %s , set globalThreshold to %dms",
						arg, PageGroup.globalThreshold));
			}

			argname = "allowedPercentOfPageGroupsFailing=";
			if (arg.startsWith(argname)) {
				mpds.allowedPercentOfPageGroupsFailing = Double
						.parseDouble(arg.substring(argname.length(), arg.length()));
				System.out.println(String.format(
						"Based on command line argument of %s , set allowedPercentOfPageGroupsFailing to %s%%", arg,
						mpds.allowedPercentOfPageGroupsFailing));
			}

			argname = "minMeasurements=";
			if (arg.startsWith(argname)) {
				PageGroup.minMeasurements = Integer.parseInt(arg.substring(argname.length(), arg.length()));
				System.out.println(
						String.format("Based on command line argument of %s , set minumum measurements to to %s", arg,
								mpds.baselineMultiplier));
			}

			argname = "queryModification=";
			if (arg.startsWith(argname)) {
				mpds.queryLineAdder = arg.substring(argname.length(), arg.length());
				System.out.println(
						String.format("Based on command line argument of %s , set query modification string to to %s",
								arg, mpds.queryLineAdder));
			}
			
		}
	}
/**
 * Parses the command line arguments related to authentication and mPulse API access
 * @param myArgs The list of all arguments. It will pull out the authentication ones and use them to set values
 */
	static void parseArgumentsTosetNameUsernameAPIandPWD(String[] myArgs) {
		// System.out.println ("Number of Arguments: "+myArgs.length);

		for (String arg : myArgs) {

			String argname;

			argname = "name=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.name = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set name to %s", arg, name));
			}

			argname = "username=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.username = arg.substring(argname.length(), arg.length());
				System.out.println(
						String.format("Based on command line argument of %s , set username to %s", arg, username));
			}

			argname = "password=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.password = arg.substring(argname.length(), arg.length());
				System.out.println(
						String.format("Based on command line argument of %s , set password to %s", arg, password));
			}

			argname = "apiToken=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.apiToken = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set apiToken to %s", arg, apiToken));
			}
			
			argname = "apiKey=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.apiKey = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set apiKey to %s", arg, apiKey));
			}
			
			argname = "tenantName=";
			if (arg.startsWith(argname)) {
				MPulseJenkins.tenantName = arg.substring(argname.length(), arg.length());
				System.out.println(String.format("Based on command line argument of %s , set tenantName to %s",
						arg, MPulseJenkins.tenantName));
			}


		}

	}

}
