import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;
import edu.upenn.cit594.datamanagement.DataManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;
@SuppressWarnings("removal")

public class BasicTests {

//	private DataManager dataManager;
//
//	@BeforeEach
//	public void setUp() {
//		// Mock COVID records
//		List<CovidRecord> covidRecords = new ArrayList<>();
//		covidRecords.add(new CovidRecord("19104", LocalDateTime.of(2021, 3, 1, 12, 0), 100, 50, 200, 0, 0, 0));
//		covidRecords.add(new CovidRecord("19104", LocalDateTime.of(2021, 3, 1, 13, 0), 120, 60, 250, 0, 0, 0)); // more recent
//
//		// Mock Property records
//		List<PropertyRecord> propertyRecords = new ArrayList<>();
//		propertyRecords.add(new PropertyRecord("19104", 200000.0, 1500.0));
//		propertyRecords.add(new PropertyRecord("19104", 300000.0, 2500.0));
//
//		// Mock Population data
//		Map<String, Integer> populationData = new HashMap<>();
//		populationData.put("19104", 1000);
//
//		dataManager = new DataManager(covidRecords, propertyRecords, populationData);
//	}
//
//	@Test
//	public void testTotalPopulation() {
//		assertEquals(1000, dataManager.getTotalPopulation());
//		// test memoization by calling again
//		assertEquals(1000, dataManager.getTotalPopulation());
//	}
//
//	@Test
//	public void testVaccinationsPerCapita() {
//		Map<String, Double> partial = dataManager.getVaccinationsPerCapita("partial", LocalDate.of(2021, 3, 1));
//		assertEquals(0.12, partial.get("19104"), 0.0001);
//
//		Map<String, Double> full = dataManager.getVaccinationsPerCapita("full", LocalDate.of(2021, 3, 1));
//		assertEquals(0.06, full.get("19104"), 0.0001);
//
//		// Memoization test (should still return same values)
//		assertEquals(0.12, dataManager.getVaccinationsPerCapita("partial", LocalDate.of(2021, 3, 1)).get("19104"), 0.0001);
//	}
//
//	@Test
//	public void testAverageMarketValue() {
//		int avg = dataManager.getAverageMarketValue("19104");
//		assertEquals(250000, avg);
//
//		// Test memoization works (call again)
//		assertEquals(250000, dataManager.getAverageMarketValue("19104"));
//	}
//
//	@Test
//	public void testAverageLivableArea() {
//		int avg = dataManager.getAverageLivableArea("19104");
//		assertEquals(2000, avg);
//
//		// Test memoization works (call again)
//		assertEquals(2000, dataManager.getAverageLivableArea("19104"));
//	}
//
//	@Test
//	public void testMarketValuePerCapita() {
//		int perCapita = dataManager.getMarketValuePerCapita("19104");
//		assertEquals(500, perCapita);
//
//		// Test memoization works (call again)
//		assertEquals(500, dataManager.getMarketValuePerCapita("19104"));
//	}

	public static boolean triedToExit = false;

	/*
	 * Student code should exit by returning from main(), not by calling System.exit
	 */
	@Before
	public void blockExit() {
		System.setSecurityManager(new SecurityManager() {
			public void checkExit(int status) {
				SecurityException se = new SecurityException("Student code called System.exit");
				// se.printStackTrace();
				throw se;
			}

			public void checkPermission(java.security.Permission perm) {
			}
		});
	}

	@After
	public void resetExit() {
		System.setSecurityManager(null);
	}

	/*
	 * Note no safety is provided. This routine is expected to fail with any error
	 * or exception in the student code.
	 */
	public String runMain(String[] args, String input) throws Exception {
		PrintStream realout = System.out;
		InputStream realin = System.in;

		/* Redirect stdin and stdout */
		ByteArrayOutputStream test_output = new ByteArrayOutputStream();
		ByteArrayInputStream test_input = new ByteArrayInputStream(input.getBytes());
		System.setOut(new PrintStream(test_output));
		System.setIn(test_input);

		/* run the student main method */
//		edu.upenn.cit594.Main.main(args);

		/* Restore the actual input/output */
		System.setOut(realout);
		System.setIn(realin);

		return test_output.toString();
	}

	public List<String> extractResults(String output) throws Exception {
		BufferedReader output_reader = new BufferedReader(new StringReader(output));
		List<String> items = new ArrayList<>();

		int state = 0;
		String line;
		while ((line = output_reader.readLine()) != null) {
			if (state == 0 || state == 2) {
				if (line.equals("BEGIN OUTPUT")) {
					state = 1;
				}
			} else if (state == 1) {
				if (line.equals("END OUTPUT")) {
					state = 2;
				} else
					items.add(line);
			}
		}
		if (state != 2) {
			System.err.println("No OUTPUT blocks detected");
			return null;
		}
		return items;
	}

	/* Application must be able to run basic operations in under 2 minutes */
//	@Test(timeout = 120000)
	public void testSpeed() throws Exception {
		String results = runMain(new String[] { "--log=speed_test.log", "--covid=covid_data.json",
				"--properties=properties.csv", "--population=population.csv" }, "2\n0\n");
		// System.out.println("raw output:\n" + results +"end of raw output\n");
		List<String> lResults = extractResults(results);
		assertFalse(lResults == null, "No assessable output detected");
		assertTrue(lResults.size() == 1,"Expected exactly one line of output");
		assertTrue(lResults.get(0).matches("^\\d+$"),"Out does not match format for operation 1");
	}

	public List<List<String>> extractResultsMulti(String output) throws Exception {
		BufferedReader output_reader = new BufferedReader(new StringReader(output));
		List<List<String>> listOfItems = new ArrayList<>();
		List<String> items = new ArrayList<>();

		int state = 0;
		String line;
		while ((line = output_reader.readLine()) != null) {
			if (state == 0 || state == 2) {
				if (line.equals("BEGIN OUTPUT"))
					state = 1;
			} else if (state == 1) {
				if (line.equals("END OUTPUT")) {
					state = 2;
					listOfItems.add(items);
					items = new ArrayList<>();
				} else
					items.add(line);
			}
		}
		if (state != 2) {
			System.err.println("No OUTPUT blocks detected");
			return null;
		}
		return listOfItems;
	}

	/* check for errors when running multiple times */
	@Timeout(value = 20000)
	public void testTwice() throws Exception {
		String result1 = runMain(
				new String[] { "--covid=covid_data.json", "--population=population.csv", "--log=small_test1.log" },
				"3\nfull\n2021-11-05\n0\n");
		String result2 = runMain(new String[] { "--covid=covid_data.csv", "--properties=downsampled_properties.csv",
				"--population=population.csv" }, "3\nfull\n2021-11-05\n0\n");

		Set<String> sResult1 = new HashSet<>(extractResults(result1));
		Set<String> sResult2 = new HashSet<>(extractResults(result2));

		assertTrue(sResult1.equals(sResult2),"Repeated execution failed");

		/*
		 * This only checks the rough line formatting, not the exact format and not the
		 * values be sure to write more tests of your own
		 */
		for (String line : sResult1) {
			assertTrue(line.matches("^\\d+ (0|[\\d\\.]+)$"), "bad line " + line);
		}
	}

	/* This one invokes main 7 times and might take a while. */
	@Timeout(value = 600000)
	public void testActivities() throws Exception {
		System.gc();
		System.out.println("Current memory used (MiB): " + (Runtime.getRuntime().totalMemory() >> 20));
		String[] args = new String[] { "--log=activities.test.log.txt", "--covid=covid_data.csv",
				"--properties=properties.csv", "--population=population.csv" };
		String[] activities = new String[] { "1", "2", "3\nfull\n2021-05-01", "4\n19149", "5\n19149", "6\n19149" };
		String results = runMain(args, Stream.of(activities).collect(Collectors.joining("\n")) + "\n0\n");
		var mResults1 = extractResultsMulti(results);
		List<List<String>> mResults2 = new ArrayList<>();
		for (String act : activities) {
			mResults2.add(extractResults(runMain(args, act + "\n0\n")));
		}
		assertTrue(mResults1.equals(mResults2),"Output differed");
		System.out.println("Current memory used (MiB): " + (Runtime.getRuntime().totalMemory() >> 20));
		System.out.println("Max memory used (MiB): " + (Runtime.getRuntime().maxMemory() >> 20));
	}
}
