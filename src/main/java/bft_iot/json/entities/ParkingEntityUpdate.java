package bft_iot.json.entities;

public class ParkingEntityUpdate extends FiwareEntity {
    public Occupancy occupancy;

    public ParkingEntityUpdate() {
        this.occupancy = new Occupancy();
    }

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public String toString() {
        return String.format("{'occupancy':%s}", occupancy == null ? "null" : occupancy.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        int this_ratio = this.occupancy.value;
        int other_ratio = ((ParkingEntityUpdate)other).occupancy.value;
        return this_ratio == other_ratio;
    }

    public class Occupancy {
        public int value;
        public String type = "Integer";

        public String toString() {
            return String.format("{'value':%d, 'type':'%s'}", value, type);
        }

    }


}
