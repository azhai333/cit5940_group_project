package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;
import java.util.*;
import java.util.function.ToDoubleFunction;

public class PropertyAverageCalculator {
    private PropertyAverageStrategy strategy;
    private List<PropertyRecord> propertyRecords;

    private final Map<String, Integer> memoizedResults = new HashMap<>();
    private ToDoubleFunction<PropertyRecord> currentExtractor;

    public PropertyAverageCalculator(List<PropertyRecord> propertyRecords) {
        this.propertyRecords = propertyRecords;
    }

    public void setStrategy(PropertyAverageStrategy strategy, ToDoubleFunction<PropertyRecord> extractor) {
        this.strategy = strategy;
        this.currentExtractor = extractor;
        memoizedResults.clear();
    }

    public int calculate(String zip) {
        if (strategy == null) throw new IllegalStateException("Strategy not set.");
        if (memoizedResults.containsKey(zip)) return memoizedResults.get(zip);
        int result = strategy.computeAverage(zip, propertyRecords);
        memoizedResults.put(zip, result);
        return result;
    }

    public static PropertyAverageCalculator createWithMarketValue(List<PropertyRecord> propertyRecords) {
        PropertyAverageCalculator calc = new PropertyAverageCalculator(propertyRecords);
        calc.setStrategy(new GenericPropertyAverageStrategy(PropertyRecord::getMarketValue), PropertyRecord::getMarketValue);
        return calc;
    }

    public static PropertyAverageCalculator createWithLivableArea(List<PropertyRecord> propertyRecords) {
        PropertyAverageCalculator calc = new PropertyAverageCalculator(propertyRecords);
        calc.setStrategy(new GenericPropertyAverageStrategy(PropertyRecord::getTotalLivableArea), PropertyRecord::getTotalLivableArea);
        return calc;
    }
}