
/**
 * A pageGroup object holds the name of a PageGroup, as well as the response
 * times of the baseline and test load times, and acceptable performance
 * dynamicThreshold. You can then query to find if a pageGroup has exceeded it's
 * dynamicThreshold and would be consider failed status. (Note: It will also
 * return 'warn' status if a test data exceeds baseline but is less than
 * dynamicThreshold.
 * 
 * @author mikeostenberg
 */
public class PageGroup {

	static int globalThreshold = 0; // You can set a generic global dynamicThreshold here. It will be used if no
									// dynamic or manual dynamicThreshold is set.
	static int minMeasurements = 1; // The minumum number of test measurements allowed in order to do a comparison.
									// If measurements<minMeasurements status will return 'TooFew'
	private double dynamicThreshold; // Threshold time will be dynamicThreshold*baselineTime if a baseline time
										// period was set.

	int testPageLoadTime, testBeaconCount;
	int baselinePageLoadTime, baselineBeaconCount;
	private int manualThresholdTime; //
	static int manualThresholdCount = 0;
	double testMoE, baselineMoE;
	boolean isStdDevMode; //Indicates if we're calculating threshold based on # of stdDeviations
	String pageGroupName;

	/**
	 *
	 * @return returns a string value of either 'PASS', 'FAIL' or 'WARN', the the
	 *         test data is less than baseline, or above baseline*dynamicThreshold
	 *         or above baseline.
	 */

	public String getStatus() {
		if (testBeaconCount < minMeasurements) {
			return "Too Few";
		}
		if (getThresholdTime() == 0) {
			return "";
		}
		if (testPageLoadTime < getThresholdTime()) {
			return "PASS";
		}
		if (testPageLoadTime < ((testPageLoadTime + getThresholdTime()) / 2)) {
			return "WARN";
		}
		if (testPageLoadTime >= getThresholdTime()) {
			return "FAIL";
		}
		return ("UNDEFINED");
	}

	/**
	 *
	 * @param pageGroupName
	 *            Initiates a pageGroup by just defining it's name.
	 */

	public PageGroup(String pageGroupName) {
		this.pageGroupName = pageGroupName;
	}

	@Override
	public String toString() {
		return String.format(
				"Name: %s, Time(ms):%3d MoE:%3.2f, Beacon Count:%3d BaselineTime:%3d  BaselineMoE:%3.2f  BaselineCount:%3d",
				pageGroupName, testPageLoadTime, testMoE, testBeaconCount, baselinePageLoadTime, testMoE,
				baselineBeaconCount);
	}

	/**
	 *
	 * @param threshold
	 *            Sets the performance dynamicThreshold for the pageGroup.
	 */
	public void setDynamicThreshold(double threshold) {
		this.dynamicThreshold = threshold;
	}

	public String getThresholdType() {
		if (this.manualThresholdTime > 0) {
			return "(m)";
		} // Put this text in the HTML table to indicate a manually set threshold.
		if (this.baselinePageLoadTime > 0 && this.isStdDevMode==true) {
			return "(&sigma;)";
		}		
		if (this.baselinePageLoadTime > 0 && this.isStdDevMode!=true) {
			return "";
		} // If it's dynamic, don't bother putting any additional text in the cell.
		if (PageGroup.manualThresholdCount > 0 && PageGroup.globalThreshold > 0) {
			return "(g)";
		} // If there's no manual or dynamic threshold, use the global threshold.
		return "-"; // Some page groups may not have a threshold if only manual was used for a few
					// page groups
	}

	/**
	 *
	 * @param manualThreshold
	 *            Sets the performance dynamicThreshold for the pageGroup.
	 */
	public void setManualThreshold(double manualThreshold) {
		this.manualThresholdTime = (int) manualThreshold; // Convert from seconds to milliseconds
	}

	public float getBaselineStdDev() {
		return (float) (this.baselineMoE*Math.sqrt(this.baselineBeaconCount)/1.96);
	}
	/**
	 * @return the thresholdTime
	 */
	public int getThresholdTime() {
		if (this.manualThresholdTime > 0) {
			return (int) this.manualThresholdTime;
		} // Use the manual instread of dynamicThreshold if it was set.
		if (this.baselinePageLoadTime > 0 && this.testMoE > 0) {
			// return (int) ((this.baselinePageLoadTime + this.baselineMoE) *
			// this.dynamicThreshold + this.testMoE); // This would add MoE into
			// calculations
			if (this.isStdDevMode == true) {
				return (int) ((this.baselinePageLoadTime) + this.dynamicThreshold * this.getBaselineStdDev());
			} else {
				return (int) ((this.baselinePageLoadTime) * this.dynamicThreshold);
			}
		} // Calculate dynamic if baselinePageLoadTime was set use the dynamic
		return PageGroup.globalThreshold;
	}

	/**
	 * @param thresholdTime
	 *            the thresholdTime to set
	 */
	// public void setThresholdTime(int thresholdTime) {
	// this.thresholdTime = thresholdTime;
	// }
}
