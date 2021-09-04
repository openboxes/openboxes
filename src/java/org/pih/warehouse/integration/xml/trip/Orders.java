package org.pih.warehouse.integration.xml.trip;

import javax.xml.bind.annotation.XmlElement;

public class Orders {

    private String orderId;
    @XmlElement(name = "OrderID")

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    private String extOrderId;
    @XmlElement(name = "EXTOrderID")
    public String getExtOrderId() {
        return extOrderId;
    }
    public void setExtOrderId(String extOrderId) {
        this.extOrderId = extOrderId;
    }

    public String toString(){
        return "TrackingId:"+this.orderId+", InternalOrderId:"+extOrderId;
    }

}
