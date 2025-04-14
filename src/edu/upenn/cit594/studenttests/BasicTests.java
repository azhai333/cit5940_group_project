import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;
import edu.upenn.cit594.datamanagement.DataManager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class BasicTests {

	private DataManager dataManager;

	@BeforeEach
	public void setUp() {
		// Mock COVID records
		List<CovidRecord> covidRecords = new ArrayList<>();
		covidRecords.add(new CovidRecord("19104", LocalDateTime.of(2021, 3, 1, 12, 0), 100, 50, 200, 0, 0, 0));
		covidRecords.add(new CovidRecord("19104", LocalDateTime.of(2021, 3, 1, 13, 0), 120, 60, 250, 0, 0, 0)); // more recent

		// Mock Property records
		List<PropertyRecord> propertyRecords = new ArrayList<>();
		propertyRecords.add(new PropertyRecord("19104", 200000.0, 1500.0));
		propertyRecords.add(new PropertyRecord("19104", 300000.0, 2500.0));

		// Mock Population data
		Map<String, Integer> populationData = new HashMap<>();
		populationData.put("19104", 1000);

		dataManager = new DataManager(covidRecords, propertyRecords, populationData);
	}

	@Test
	public void testTotalPopulation() {
		assertEquals(1000, dataManager.getTotalPopulation());
		// test memoization by calling again
		assertEquals(1000, dataManager.getTotalPopulation());
	}

	@Test
	public void testVaccinationsPerCapita() {
		Map<String, Double> partial = dataManager.getVaccinationsPerCapita("partial", LocalDate.of(2021, 3, 1));
		assertEquals(0.12, partial.get("19104"), 0.0001);

		Map<String, Double> full = dataManager.getVaccinationsPerCapita("full", LocalDate.of(2021, 3, 1));
		assertEquals(0.06, full.get("19104"), 0.0001);

		// Memoization test (should still return same values)
		assertEquals(0.12, dataManager.getVaccinationsPerCapita("partial", LocalDate.of(2021, 3, 1)).get("19104"), 0.0001);
	}

	@Test
	public void testAverageMarketValue() {
		int avg = dataManager.getAverageMarketValue("19104");
		assertEquals(250000, avg);

		// Test memoization works (call again)
		assertEquals(250000, dataManager.getAverageMarketValue("19104"));
	}

	@Test
	public void testAverageLivableArea() {
		int avg = dataManager.getAverageLivableArea("19104");
		assertEquals(2000, avg);

		// Test memoization works (call again)
		assertEquals(2000, dataManager.getAverageLivableArea("19104"));
	}

	@Test
	public void testMarketValuePerCapita() {
		int perCapita = dataManager.getMarketValuePerCapita("19104");
		assertEquals(500, perCapita);

		// Test memoization works (call again)
		assertEquals(500, dataManager.getMarketValuePerCapita("19104"));
	}
}
