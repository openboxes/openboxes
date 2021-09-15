package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class TripOrderDetails {

    public TripOrderDetails() {
    }

    public TripOrderDetails(ArrayList<Orders> orders) {
        this.orders = orders;
    }

    @XmlElement(name = "Orders")
    public ArrayList<Orders> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Orders> orders) {
        this.orders = orders;
    }

    private ArrayList<Orders> orders;

}
