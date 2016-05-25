package nl.tudelft.exchange.student.locationtracker.filter;

import java.util.Map;

import nl.tudelft.exchange.student.locationtracker.filter.data.AccessPoint;

/**
 * Created by Piotr on 2016-05-25.
 */
public class BayesianFilter {

    private Map<String, AccessPoint> accessPointMap;

    public BayesianFilter(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
    }

    public Map<String, AccessPoint> getAccessPointMap() {
        return accessPointMap;
    }

    public void setAccessPointMap(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
    }
}
