package bft_iot.json.entities;

public class TrashSensorEntity extends FiwareCreateEntity {
    public Location location;
    public Ratio ratio;

    public TrashSensorEntity() {
        this.location = new Location();
        this.ratio = new Ratio();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Ratio getRatio() {
        return ratio;
    }

    public void setRatio(Ratio ratio) {
        this.ratio = ratio;
    }

    public String toString() {
        return String.format("{'id':'%s', 'type':'%s', 'location':%s, 'ratio':%s}", id, type, location == null ? "null" : location.toString(), ratio == null ? "null" : ratio.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        float this_ratio = this.ratio.value;
        float other_ratio = ((TrashSensorEntity)other).ratio.value;
        return Math.abs(this_ratio - other_ratio) <= config.ratio_threshold;
    }

    public class Ratio {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }

        public void setValue(float newValue) {
            this.value = newValue;
        }
    }

    public class Location {
        private String value;
        private String type;

        public String toString() {
            return String.format("{'value':'%s', 'type':'%s'}", value, type);
        }
    }
}

