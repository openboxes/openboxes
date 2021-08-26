package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Order")
@XmlType(propOrder = {"header", "action", "knOrgDetails", "orderDetails"})
public class Order {
    private Header header;
    private String action;
    private KNOrgDetails knOrgDetails;
    private OrderDetails orderDetails;

    @XmlElement(name = "Header")
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    @XmlElement(name = "Action")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @XmlElement(name = "KNOrgDetails")
    public KNOrgDetails getKnOrgDetails() {
        return knOrgDetails;
    }

    public void setKnOrgDetails(KNOrgDetails knOrgDetails) {
        this.knOrgDetails = knOrgDetails;
    }

    @XmlElement(name = "OrderDetails")
    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(OrderDetails orderDetails) {
        this.orderDetails = orderDetails;
    }
}
