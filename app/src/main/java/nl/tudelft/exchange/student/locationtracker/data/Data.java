package nl.tudelft.exchange.student.locationtracker.data;

/**
 * Created by Piotr on 2016-05-03.
 */
public abstract class Data {

    protected long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
