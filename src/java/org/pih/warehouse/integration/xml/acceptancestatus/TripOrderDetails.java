package org.pih.warehouse.integration.xml.acceptancestatus;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class TripOrderDetails {
    private List<String> orderId;
    @XmlElement(name = "OrderID")
    public List<String> getOrderId() {
        return orderId;
    }
    public void setOrderId(List <String> orderId) {
        this.orderId = orderId;
    }
}
