package com.estacionamiento;

public enum VehicleType {
    AUTO(800),
    MOTO(500),
    CAMIONETA(1000);

    private final int ratePerBlock;

    VehicleType(int ratePerBlock) {
        this.ratePerBlock = ratePerBlock;
    }

    public int getRatePerBlock() {
        return ratePerBlock;
    }
}
