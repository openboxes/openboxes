package org.pih.warehouse.integration.xml.trip;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class TripOrderDetails {
    private List<Orders> orders;
    @XmlElement(name = "Orders")
    public List<Orders> getOrders() {
        return orders;
    }
    public void setOrders(List <Orders> orders) {
        this.orders = orders;
    }
}
