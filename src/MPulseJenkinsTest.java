import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MPulseJenkinsTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testParseArguments() {
		String [] args= {"baselineStartDate=-2weeks","baselineEndDate=-2days","baselineMultiplier=1.1","testStartDate=-3hours","testEndDate=now","globalThreshold=100",
				"apiToken=ea57e6bf-8ac5-4f7a-964d-892dd912beca","allowedPercentOfPageGroupsFailing=50.0","minMeasurements=10",
				"name=Choice Hotels", "apiKey=H45A6-DFUWB-FMVSB-XRBWW-XXPJ8","tenantName=POC - Choice Hotels","queryModification=geography?"};
		MPulseJenkins.main(args);
		//fail("Not yet implemented,sir ");
	}

	//@Test
	void testParseArguments2() {
		String [] args= {"baselineStartDate=-2weeks","baselineEndDate=-2days","baselineMultiplier=1.1","testStartDate=-3hours","testEndDate=now","globalThreshold=100",
				"apiToken=ea57e6bf-8ac5-4f7a-964d-892dd912beca","allowedPercentOfPageGroupsFailing=50.0","minMeasurements=10",
				"name=POC - H.I.S.", "apiKey=N96PU-U8A3D-XP24Z-TAFVY-2YH3X","tenantName=POC - H.I.S."};
		MPulseJenkins.main(args);
		//fail("Not yet implemented,sir ");
	}
	
}
