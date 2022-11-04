package bft_iot.json.entities;

public class TrashEntityUpdate extends FiwareEntity {
    public Ratio ratio;

    public TrashEntityUpdate() {
        this.ratio = new Ratio();
    }

    public String toString() {
        return String.format("{'ratio':%s}", ratio == null ? "null" : ratio.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        float this_ratio = this.ratio.value;
        float other_ratio = ((TrashEntityUpdate)other).ratio.value;
        return Math.abs(this_ratio - other_ratio) <= config.ratio_threshold;
    }

    public class Ratio {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }

    }


}
