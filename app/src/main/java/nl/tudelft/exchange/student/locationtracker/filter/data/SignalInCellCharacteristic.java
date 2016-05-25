package nl.tudelft.exchange.student.locationtracker.filter.data;

/**
 * Created by Piotr on 2016-05-25.
 */
public class SignalInCellCharacteristic {

    private Integer roomID;

    private Double meanSignalValue;

    private Double standardDeviationOfSignalValue;

    public SignalInCellCharacteristic(Integer roomID, Double meanSignalValue, Double standardDeviationOfSignalValue) {
        this.roomID = roomID;
        this.meanSignalValue = meanSignalValue;
        this.standardDeviationOfSignalValue = standardDeviationOfSignalValue;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public void setRoomID(Integer roomID) {
        this.roomID = roomID;
    }

    public Double getMeanSignalValue() {
        return meanSignalValue;
    }

    public void setMeanSignalValue(Double meanSignalValue) {
        this.meanSignalValue = meanSignalValue;
    }

    public Double getStandardDeviationOfSignalValue() {
        return standardDeviationOfSignalValue;
    }

    public void setStandardDeviationOfSignalValue(Double standardDeviationOfSignalValue) {
        this.standardDeviationOfSignalValue = standardDeviationOfSignalValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignalInCellCharacteristic that = (SignalInCellCharacteristic) o;

        if (roomID != null ? !roomID.equals(that.roomID) : that.roomID != null) return false;
        if (meanSignalValue != null ? !meanSignalValue.equals(that.meanSignalValue) : that.meanSignalValue != null)
            return false;
        return !(standardDeviationOfSignalValue != null ? !standardDeviationOfSignalValue.equals(that.standardDeviationOfSignalValue) : that.standardDeviationOfSignalValue != null);

    }

    @Override
    public int hashCode() {
        return roomID;
    }
}
