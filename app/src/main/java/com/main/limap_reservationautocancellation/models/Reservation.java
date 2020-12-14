package com.main.limap_reservationautocancellation.models;

import com.google.firebase.Timestamp;

public class Reservation {
    private Timestamp reserveTime;
    private String parkingLotId;
    private long parkingSpotId;
    private String userId;
    private long cost;

    public Timestamp getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(Timestamp reserveTime) {
        this.reserveTime = reserveTime;
    }

    public String getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(String parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public long getParkingSpotId() {
        return parkingSpotId;
    }

    public void setParkingSpotId(long parkingSpotId) {
        this.parkingSpotId = parkingSpotId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }
}
