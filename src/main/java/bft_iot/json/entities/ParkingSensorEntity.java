package bft_iot.json.entities;

public class ParkingSensorEntity extends FiwareCreateEntity {
    public Location location;
    public Occupancy occupancy;

    public ParkingSensorEntity() {
        this.location = new Location();
        this.occupancy = new Occupancy();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(Occupancy occupancy) {
        this.occupancy = occupancy;
    }

    public String toString() {
        return String.format("{'id':'%s', 'type':'%s', 'location':%s, 'ratio':%s}", id, type, location == null ? "null" : location.toString(), occupancy == null ? "null" : occupancy.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        int this_ratio = this.occupancy.value;
        int other_ratio = ((ParkingSensorEntity)other).occupancy.value;
        return this_ratio == other_ratio;
    }

    public class Occupancy {
        public int value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }

        public void setValue(int newValue) {
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

