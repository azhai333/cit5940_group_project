package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

// This strategy class defines how to compute the average of a specified property field for a given zip code
public class GenericPropertyAverageStrategy implements PropertyAverageStrategy {

    // Functional interface to extract a specific metric from a PropertyRecord
    private final ToDoubleFunction<PropertyRecord> metricExtractor;

    // Constructor receives a method reference to specify which metric to average
    public GenericPropertyAverageStrategy(ToDoubleFunction<PropertyRecord> metricExtractor) {
        this.metricExtractor = metricExtractor;
    }

    // Computes average of the specified metric for properties in a given zip code
    @Override
    public int computeAverage(String zip, List<PropertyRecord> properties) {
        List<PropertyRecord> filtered = properties.stream()
                .filter(r -> r.getZipCode().equals(zip))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) return 0;

        double sum = filtered.stream().mapToDouble(metricExtractor).sum();
        return (int) (sum / filtered.size());
    }
}