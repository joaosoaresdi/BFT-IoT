package bft_iot.json.entities;

public abstract class FiwareEntity {
    public abstract boolean compareTo(FiwareEntity other, BFTIoTConfig config);
}
