package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;
import java.util.List;

public interface PropertyAverageStrategy {
    int computeAverage(String zip, List<PropertyRecord> properties);
}