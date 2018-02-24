
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * If you pass in two strings (which are the JSON responses from two mPulse API
 * queries), this class will parse them to create two datasets of pageGroups
 * (called 'baseline' and 'test' dataset) and compare those two datasets to see
 * if the test dataset is more than a certain baselineMultiplier above the
 * baseline.
 * 
 * @author mikeostenberg
 */
public class MPulseDataSet {

	static Calendar getDate(String dateString) {
		Calendar returnDate = Calendar.getInstance(); // Initially sets returnDate to the current date and time.

		// 9/13/2015
		String patternString = ("^(\\d+/\\d+/\\d+)");
		Pattern p = Pattern.compile(patternString);
		Matcher m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Simple date. Found value for
			// baselineStartDate of "+m.group(0));
			SimpleDateFormat justYearFormat = new SimpleDateFormat("M/d/yyyy");
			try {
				returnDate.setTime(justYearFormat.parse(m.group(0)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
			} catch (Exception f) {
				System.out.println("Exception: " + f.getMessage());
			}

		}

		// 9:43am-9/13/2015
		patternString = ("(\\d+:\\d+..-\\d+/\\d+/\\d+)");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Date plus year. Found value for
			// baselineStartDate of "+m.group(0));
			SimpleDateFormat possibleDateFormat = new SimpleDateFormat("hh:mma-M/d/yyyy");
			try {
				returnDate.setTime(possibleDateFormat.parse(m.group(0)));
				return returnDate;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
			} catch (Exception f) {
				System.out.println("Exception: " + f.getMessage());
			}
		}

		// -3days
		patternString = ("(-\\d+)days");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Found relative value for baselineStartDate
			// of "+m.group(1));
			try {
				Integer daysBack = Integer.parseInt(m.group(1));
				// Now set to midnight
				returnDate.set(Calendar.SECOND, 0);
				returnDate.set(Calendar.MILLISECOND, 0);
				returnDate.set(Calendar.HOUR, 0);
				returnDate.set(Calendar.MINUTE, 0);

				returnDate.add(Calendar.DAY_OF_MONTH, (daysBack));
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for days back: " + f.getMessage());
			}
		}

		// -3weeks
		patternString = ("(-\\d+)weeks");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Found relative value for baselineStartDate
			// of "+m.group(1));
			try {
				Integer daysBack = 7*Integer.parseInt(m.group(1));
				// Now set to midnight
				returnDate.set(Calendar.SECOND, 0);
				returnDate.set(Calendar.MILLISECOND, 0);
				returnDate.set(Calendar.HOUR, 0);
				returnDate.set(Calendar.MINUTE, 0);

				returnDate.add(Calendar.DAY_OF_MONTH, (daysBack));
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for days back: " + f.getMessage());
			}
		}

		// -3hours
		patternString = ("(-\\d+)hours");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Found relative hour value for
			// baselineStartDate of "+m.group(1));
			try {
				Integer hoursBack = Integer.parseInt(m.group(1));
				// Now set to midnight
				returnDate.add(Calendar.HOUR, (hoursBack));
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for days back: " + f.getMessage());
			}
		}
		// -3minutes
		patternString = ("(-\\d+)minutes");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Found relative hour value for
			// baselineStartDate of "+m.group(1));
			try {
				Integer minutesBack = Integer.parseInt(m.group(1));
				// Now set to midnight
				returnDate.add(Calendar.MINUTE, (minutesBack));
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for minutes back: " + f.getMessage());
			}
		}

		// today
		patternString = ("(today)");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Datestring was set to today: "+m.group(1));
			try {
				returnDate = Calendar.getInstance(); // Start by refresh setting
														// it to current time
				// Now set to midnight
				returnDate.set(Calendar.SECOND, 0);
				returnDate.set(Calendar.MILLISECOND, 0);
				returnDate.set(Calendar.HOUR, 0);
				returnDate.set(Calendar.MINUTE, 0);
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for days back: " + f.getMessage());
			}
		}

		// now
		patternString = ("(now)");
		p = Pattern.compile(patternString);
		m = p.matcher(dateString);
		if (m.find()) {
			// System.out.println ("Datestring was set to now: "+m.group(1));
			try {
				returnDate = Calendar.getInstance(); // Start by refresh setting
														// it to current time
				return returnDate;
			} catch (Exception f) {
				System.out.println("Exception for days back: " + f.getMessage());
			}
		}

		return returnDate;
	}

	String baseline = null;
	String timezone = "US%2FEastern";
	String baselineQueryString, testDataQueryString, testDomain, baselineDomain;
	Calendar baselineStartDate, baselineEndDate, testStartDate, testEndDate;
	MPulseApplication mps = null;
	Double allowedPercentOfPageGroupsFailing = null; // The overall Threshold for the job,
	String queryLineAdder = "page-groups?"; // This is part of the query to mPulse, indicating what types of values to
											// return back. We'll modify for different situations (e.g. "browsers" or
											// "browsers&page-group=home" etc
	// Map<String, PageGroup> pageGroups = new TreeMap<String, PageGroup>();
	HashMap<String, PageGroup> pageGroups = new HashMap<>();
	TreeMap<String, PageGroup> sortedPageGroups = sortMapByValue(pageGroups);

	public static TreeMap<String, PageGroup> sortMapByValue(HashMap<String, PageGroup> map) {
		Comparator<String> comparator = new ValueComparator(map);
		// TreeMap is a map sorted by its keys.
		// The comparator is used to sort the TreeMap by keys.
		TreeMap<String, PageGroup> result = new TreeMap<>(comparator);
		result.putAll(map);
		return result;
	}

	private String queryString = null;

	JSONObject testJobj, baselineJobj;

	JSONArray testPageGroups, baselinePageGroups;
	private String testResults = null;

	double baselineMultiplier;

	public MPulseDataSet() {
	};

	public MPulseDataSet(MPulseApplication mps) {
		this.mps = mps;
	};

	/**
	 *
	 * @param testResults
	 * @param baseline
	 * @param threshold
	 *            How much the baselineMultiplier may exceed baseline and still pass
	 *            (Double)
	 */
	public MPulseDataSet(String testResults, String baseline, Double threshold) {
		this.testResults = testResults;
		this.baseline = baseline;
		this.baselineMultiplier = threshold;
		parseJSONStringToCreatePageGroups();
	}

	/**
	 *
	 * @return How many items failed to meet their threshold.
	 */
	public int getFailCount() {
		int failCount = 0;
		for (Entry<String, PageGroup> myItem : pageGroups.entrySet()) {
			PageGroup current = myItem.getValue();
			if (current.getStatus() == "FAIL") {
				failCount++;
			}
		}

		return failCount;
	}

	public int getQualifiedPGCount() {
		int measuredPGCount = 0;
		for (Entry<String, PageGroup> myItem : pageGroups.entrySet()) {
			PageGroup current = myItem.getValue();
			if (current.getStatus() == "FAIL" || current.getStatus() == "PASS" || current.getStatus() == "WARN") {
				measuredPGCount++;
			}
		}

		return measuredPGCount;
	}

	// Method for sorting the TreeMap based on number of beacons for the group

	String getOverallStatus() {
		if (((float) getFailCount() / getQualifiedPGCount()) > (this.allowedPercentOfPageGroupsFailing / 100)) {
			StringBuilder sb;
			sb = new StringBuilder("FAIL");// :%d/%d"+getFailCount() +getTotalTransactions());
			return sb.toString();
		} else {
			StringBuilder sb;
			sb = new StringBuilder("PASS");// :%d/%d"+ Integer.toString(getFailCount())
											// +"/"+Integer.toString(getTotalTransactions()));
			return sb.toString();
		}
	}

	String getHTMLSummary() {
		String tableHeader="Akamai mPulse Real Browser Performance Measurements";
		String myName = mps.name; if (myName.length()>0) {tableHeader=tableHeader+" Name: "+ myName ;}
		String myTenant=mps.tenantName;if (myTenant.length()>0) {tableHeader=tableHeader+" Tenant: "+ myTenant ;}
		StringBuilder sb = new StringBuilder("<!DOCTYPE html><html>\n" + "<head>\n\t"
				+ "<style>.FAIL{background-color: #FFA0A0;} .PASS{background-color: #CEFFE6;} .WARN{background-color: yellow}"
				+ ".OVERALLPASS{background-color:green;} .OVERALLFAIL{background-color:red}"
				+ ".vertical-text {	display: inline-block;	overflow: hidden;	width: 1.5em;}"
				+ ".vertical-text__inner {	display: inline-block;	white-space: nowrap;	line-height: 1.5;	transform: translate(0,100%) rotate(-90deg);	transform-origin: 0 0;}"
				+ ".vertical-text__inner:after {	content: \"\";	display: block;	margin: -1.5em 0 100%;}"
				+ ".tableheader {background-color: #D8D8D8;}" + "</style>\n"
				+ "<title> mPulse PageLoad Time Results </title>" + "</head>\n<body>\n"
				+ "<table border=\"1\"><caption>"+tableHeader+"\"</caption>\n"
				+ "<tr class=\"tableheader\"><th>Page Group Name</th><th colspan=\"3\">Baseline Data</th><th colspan=\"3\">Test Data</th><th>Threshold</th><th>Status</th><th>Overall Status</th></tr>\n"
				+ "<tr class=\"tableheader\"><td>-</td><td>PageLoadTime</td><td>Measurements</td><td>MoE</td><td>PageLoadTime</td><td>Measurements</td><td>MoE</td><td>"
				+ " " + baselineMultiplier + "* baseline</td><td>-</td><td>" + this.allowedPercentOfPageGroupsFailing
				+ " % of PageGroups may fail</td>\n");

		Boolean isFirstRow = true;
		String overallStatusCell;
		TreeMap<String, PageGroup> sortedMap = sortMapByValue(pageGroups);

		for (Map.Entry<String, PageGroup> myItem : sortedMap.entrySet()) {
			PageGroup current = myItem.getValue();
			if (isFirstRow) {
				overallStatusCell = String.format("<td rowspan=\"%d\" class=\"OVERALL" + getOverallStatus() + "\">"
						+ "<div class=\"vertical-text\"><div class=\"vertical-text__inner\"> OVERALL_%s: %3.2f%% Failed (%d of %d)</div></div></td>",
						getTotalTransactions(), getOverallStatus(),
						(float) getFailCount() * 100 / getQualifiedPGCount(), getFailCount(), getQualifiedPGCount());
			} else {
				overallStatusCell = "";
			}
			sb.append(String.format(
					"<tr><td>%s</td><td>%3.3f</td><td>%3d</td><td>%3.3f</td><td>%3.3f</td><td>%3d</td><td>%3.3f</td><td>%3.3f%s</td><td class=\"%s\">%s</td>%s</tr>\n",
					current.pageGroupName, (float) current.baselinePageLoadTime / 1000, current.baselineBeaconCount,
					(float) current.baselineMoE / 1000, (float) current.testPageLoadTime / 1000,
					current.testBeaconCount, (float) current.testMoE / 1000, (float) current.getThresholdTime() / 1000,
					current.getThresholdType(), current.getStatus(), current.getStatus(), overallStatusCell));
			isFirstRow = false;
		}
		sb.append("</table>\n");

		sb.append("<table border=\"1\"> <caption> Data Source Info </caption>\n)"
				+ "<tr class=\"tableheader\"><th colspan=\"3\">BASELINE</th><th colspan=\"3\">Test Data</th></tr>\n"
				+ "<tr class=\"tableheader\"><td>Start Date</td><td>End Date</td><td>Domain</td><td>Start Date</td><td>End Date</td><td>Domain</td></tr>\n");
		sb.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
				(baselineStartDate != null ? baselineStartDate.getTime().toString() : "-"),
				(baselineEndDate != null ? baselineEndDate.getTime().toString() : "-"), baselineDomain,
				testStartDate.getTime().toString(), testEndDate.getTime().toString(), testDomain));
		sb.append("</table>");
		sb.append("</body>\n</html>");
		return sb.toString();
	}

	String getJenkinsXMLSummary() {
		return "METHOD getJenkinsXMLSummary. Please update return value.";
	}

	/**
	 * Utility method used to convert any of various dateString formats into an
	 * actual calendar object. Date formats can be either explicit or relative date
	 * formats. Currently supported date formats: Explicit: MM/DD/YY ,
	 * hh:mmAM-MM/DD/YY, HH:mm:ssAM-MM/DD/YY Relative:-3days, -3d , -3h, -3hours,
	 * -15minutes
	 * 
	 * @param dateString
	 *            A string in any of the following formats: MM/DD/YY ,
	 *            HH:mm:ss-MM/DD/YY, HH:mm:ssAM-MM/DD/YY
	 * @return A calendar object representing the date.
	 **/

	Integer getTotalTransactions() {
		return pageGroups.size();
	}

	String getXMLSummary() {
		// $junitxml='<?xml version="1.0" encoding="UTF-8"?>'."\n";
		// $junitxml.="<testsuite tests=\"$numTests\" errors=\"$compErrors\"
		// timestamp=\"$timestamp\">\n";
		// $junitxml.="\t<testcase name=\"Link to detailed Results:
		// $soastaUrl\/Central\?initResultsTab=$resultID\"
		// classname=\"Performance\" time=\"0\" />\n";
		// <On FAIL>: $junitxml.="\t<testcase name=\"$SLA avg should not exceed
		// $SLA{$SLA}\" classname=\"Performance\" time=\"$actual{$SLA}\" />\n";
		// <on PASS>: $junitxml.="\t<testcase name=\"$SLA avg should not exceed
		// $SLA{$SLA}\" classname=\"Performance\" time=\"$actual{$SLA}\">
		// \n\t\t<failure type=\"performance\">
		// $message</failure>\n\t</testcase>\n";
		// $junitxml.='</testsuite>';
		Calendar now = Calendar.getInstance();
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append(String.format("<testsuite tests=\"%2d\" errors=\"%2d\" timestamp=\"%tD\" overall=\"%s\" >\n", pageGroups.size(),
				getFailCount(), now,this.getOverallStatus()));
		for (Entry<String, PageGroup> myItem : pageGroups.entrySet()) {
			PageGroup current = myItem.getValue();
			if (current.getStatus() == "FAIL") {
				// String failureMessage = String.format("message='mPulse Query
				// String for Baseline Data: %s\"\n\t\t\t message=\"mPulse Query
				// String for Test Data: %s
				// \'\n",PageGroup.baselineQueryString,PageGroup.testDataQueryString);
				String failureMessage = String.format(
						"PageGroup: %s , Threshold: %3.3f, Actual PageLoadTime: %3.3f \n\t\t\tBASELINE: PageLoadTime:  %3.3f sec, Measurements: %3d Moe: %3.3f sec \n\t\t\tTEST: PageLoadTime: %3.3f sec, Measurements: %3d , Moe: %3.3f sec",
						current.pageGroupName, (float) current.getThresholdTime() / 1000,
						(float) current.testPageLoadTime / 1000, (float) current.baselinePageLoadTime / 1000,
						current.baselineBeaconCount, current.baselineMoE / 1000,
						(float) current.testPageLoadTime / 1000, current.testBeaconCount, current.testMoE / 1000);

				sb.append(String.format(
						"\t<testcase name=\"%s PageLoad time should not exceed %3.3f sec\" classname=\"Performance\" time=\"%3.3f\" >\n\t\t<failure type=\"performance\"> \n\t\t\t%s\n\t\t</failure>\n\t</testcase>\n",
						current.pageGroupName, ((double) current.getThresholdTime()) / 1000,
						(float) current.testPageLoadTime / 1000, failureMessage));
			} else {
				sb.append(String.format(
						"\t<testcase name=\"%s time should not exceed %3.3f sec\" classname=\"Performance\" time=\"%3.3f\" />\n",
						current.pageGroupName, (double) current.getThresholdTime() / 1000,
						(float) current.testPageLoadTime / 1000));
			}
		}
		sb.append("</testsuite>\n");
		return sb.toString();
	}

	void parseJSONStringToCreatePageGroups() {
		testJobj = new JSONObject(testResults);
		// = (String []) testJobj.get("data");
		testPageGroups = testJobj.getJSONArray("data");
		for (int i = 0; i < testPageGroups.length(); i++) {
			// System.out.println("Keyword: " +
			// testPageGroups.getJSONArray(i).get(0));

			String pageGroupName = testPageGroups.getJSONArray(i).get(0).toString();
			PageGroup myPGD;
			if (!pageGroups.containsKey(pageGroupName)) {
				myPGD = new PageGroup(pageGroupName);
				pageGroups.put(pageGroupName, myPGD);
			} else {
				myPGD = pageGroups.get(pageGroupName);
			}

			myPGD.setDynamicThreshold(baselineMultiplier);
			myPGD.testPageLoadTime = (Integer.parseInt(testPageGroups.getJSONArray(i).get(1).toString()));
			myPGD.testMoE = (Double.parseDouble(testPageGroups.getJSONArray(i).get(2).toString()));
			myPGD.testBeaconCount = (Integer.parseInt(testPageGroups.getJSONArray(i).get(3).toString()));
			// System.out.println ("My PGD"+myPGD.toString());

		}

		if (this.baselineStartDate != null && this.baselineEndDate != null && this.baselineMultiplier > 0) {
			baselineJobj = new JSONObject(baseline);
			baselinePageGroups = baselineJobj.getJSONArray("data");
			for (int i = 0; i < this.baselinePageGroups.length(); i++) {
				String pageGroupName = baselinePageGroups.getJSONArray(i).get(0).toString();
				System.out.println("PageGroup Name is " + pageGroupName);
				if (pageGroups.containsKey(pageGroupName)) {
					PageGroup myPG = pageGroups.get(pageGroupName);
					myPG.baselinePageLoadTime = (Integer
							.parseInt(baselinePageGroups.getJSONArray(i).get(1).toString()));
					myPG.baselineMoE = (Double.parseDouble(baselinePageGroups.getJSONArray(i).get(2).toString()));
					System.out.println("Value is " + baselinePageGroups.getJSONArray(i).get(3).toString());
					//myPG.baselineBeaconCount = (Integer.parseInt(baselinePageGroups.getJSONArray(i).get(3).toString()));
					myPG.baselineBeaconCount = new BigDecimal((baselinePageGroups.getJSONArray(i).get(3).toString())).intValue(); //See if this handles scientific notation
					
				}
			}
		}
	}

	void setBaseline() {
		if (this.baselineStartDate == null) {
			System.out.println("No baseline Start set\n.");
			return;
		}
		String startDate = new SimpleDateFormat("YYYY-MM-dd").format(this.baselineStartDate.getTime());
		String endDate = new SimpleDateFormat("YYYY-MM-dd").format(this.baselineEndDate.getTime());

		// If baseline domain was defined then use it,otherwise drop it out of query
		// string.
		if (baselineDomain != null && baselineDomain.length() > 0) {
			queryString = String.format(
					"%sformat=json&custom-dimension-subdomain=%s&date-comparator=Between&date-start=%s&date-end=%s",
					this.queryLineAdder, baselineDomain, startDate, endDate, timezone);
		} else {
			queryString = String.format("%sformat=json&date-comparator=Between&date-start=%s&date-end=%s",
					this.queryLineAdder, startDate, endDate);
		}
		System.out.println("Baseline Data query string is: " + queryString);
		baseline = mps.mPulseQuery(queryString);
	}

	void setTestData() {
		String startDate = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'").format(this.testStartDate.getTime());// was:
																													// YYYY-MM-dd'T'HH:mm:ss'Z'"
		String endDate = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'").format(this.testEndDate.getTime());

		// If the test domain was defined, then use it, otherwise, drop it out of the
		// query string
		if (testDomain != null && testDomain.length() > 0) {
			queryString = String.format(
					"%sformat=json&custom-dimension-subdomain=%s&date-comparator=Between&date-start=%s&date-end=%s&timezone=%s",
					this.queryLineAdder, testDomain, startDate, endDate, timezone); // Today's values for
		} else {
			queryString = String.format("%sformat=json&date-comparator=Between&date-start=%s&date-end=%s&timezone=%s",
					this.queryLineAdder, startDate, endDate, timezone); // Today's values for
		}
		System.out.println("Test     Data query string is: " + queryString);
		testResults = mps.mPulseQuery(queryString);
	}

	void writeCSVSummaries() {

		StringBuilder pageGroupNames = new StringBuilder("");
		StringBuilder baselineTimes = new StringBuilder("");
		StringBuilder testTimes = new StringBuilder("");
		StringBuilder baselineMoE = new StringBuilder("");
		StringBuilder testMoE = new StringBuilder("");
		StringBuilder baselineMeasurements = new StringBuilder("");
		StringBuilder testMeasurements = new StringBuilder("");

		for (Entry<String, PageGroup> myItem : pageGroups.entrySet()) {
			PageGroup current = myItem.getValue();
			pageGroupNames.append(current.pageGroupName + ",");
			baselineTimes.append(String.format("%3.3f,", (float) current.baselinePageLoadTime / 1000));
			testTimes.append(String.format("%3.3f,", (float) current.testPageLoadTime / 1000));
			baselineMoE.append(String.format("%3.3f,", (float) current.baselineMoE / 1000));
			testMoE.append(String.format("%3.3f,", (float) current.testMoE / 1000));
			baselineMeasurements.append(String.format("%3d,", current.baselineBeaconCount));
			testMeasurements.append(String.format("%3d,", current.testBeaconCount));
		}

		try {
			PrintWriter writer = new PrintWriter("baselinePageLoadTimes.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(baselineTimes.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			PrintWriter writer = new PrintWriter("testPageLoadTimes.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(testTimes.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			PrintWriter writer = new PrintWriter("baselineMeasurements.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(baselineMeasurements.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			PrintWriter writer = new PrintWriter("testMeasurements.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(testMeasurements.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			PrintWriter writer = new PrintWriter("baselineMoEs.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(baselineMoE.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			PrintWriter writer = new PrintWriter("testMoEs.csv", "UTF-8");
			writer.println(pageGroupNames.toString());
			writer.println(testMoE.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

// This is the comparator algorightm that allows us to sort the collection of
// items based on number of beacons..
class ValueComparator implements Comparator<String> {

	HashMap<String, PageGroup> map = new HashMap<>();

	public ValueComparator(HashMap<String, PageGroup> map) {
		this.map.putAll(map);
	}

	@Override
	public int compare(String s1, String s2) {
		PageGroup pg1 = map.get(s1);
		PageGroup pg2 = map.get(s2);

		if (pg1.testBeaconCount >= pg2.testBeaconCount) {
			return -1;
		} else {
			return 1;
		}

	}
}