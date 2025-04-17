package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class GenericPropertyAverageStrategy implements PropertyAverageStrategy {

    private final ToDoubleFunction<PropertyRecord> metricExtractor;

    public GenericPropertyAverageStrategy(ToDoubleFunction<PropertyRecord> metricExtractor) {
        this.metricExtractor = metricExtractor;
    }

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