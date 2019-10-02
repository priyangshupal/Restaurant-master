package com.project.restaurant.list_items;

public class StatusListItem {

    private String OrderId,OrderAmount;
    private String estimatedTime;
    private boolean oPlaced, oConfirmed, oReady;

    public StatusListItem(String orderId, String orderAmount, String estimatedTime, boolean oPlaced, boolean oConfirmed, boolean oReady) {
        OrderId = orderId;
        OrderAmount = orderAmount;
        this.estimatedTime = estimatedTime;
        this.oPlaced = oPlaced;
        this.oConfirmed = oConfirmed;
        this.oReady = oReady;
    }

    public String getOrderId() {
        return OrderId;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public boolean isoPlaced() {
        return oPlaced;
    }

    public boolean isoConfirmed() {
        return oConfirmed;
    }

    public boolean isoReady() {
        return oReady;
    }

    public String getOrderAmount() {
        return OrderAmount;
    }
}
