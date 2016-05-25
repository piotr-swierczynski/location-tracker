package nl.tudelft.exchange.student.locationtracker.filter.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Piotr on 2016-05-25.
 */
public class AccessPoint {

    private String macAddress;

    private Map<Integer, SignalInCellCharacteristic> cellsCharacteristicMap = new HashMap<>();

    public AccessPoint(String macAddress) {
        this.macAddress = macAddress;
    }

    public Map<Integer, SignalInCellCharacteristic> getCellsCharacteristicMap() {
        return cellsCharacteristicMap;
    }

    public void setCellsCharacteristicMap(Map<Integer, SignalInCellCharacteristic> cellsCharacteristicMap) {
        this.cellsCharacteristicMap = cellsCharacteristicMap;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
