package com.main.limap_reservationautocancellation.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

public class ParkingLot {
    private String docId;
    private String name;
    private String description;
    private GeoPoint location;
    private Map<String, Long> price;
    private List<ParkingSpot> spots;

    public int getTotalNumOfSpots() {
        return spots.size();
    }

    public int getAvailableNumOfSpots() {
        int count = 0;
        for (ParkingSpot s : spots) {
            if(s.getStatus().equals("free")){
                count++;
            }
        }
        return count;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Map<String, Long> getPrice() {
        return price;
    }

    public void setPrice(Map<String, Long> price) {
        this.price = price;
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpot> spots) {
        this.spots = spots;
    }
}
