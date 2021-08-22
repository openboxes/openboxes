package org.pih.warehouse.xml.execution;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"orderId","status","dateTime", "geoData"})
public class ExecutionStatus {

    private String orderId;
    private String status;
    private String dateTime;
    private GeoData geoData;

    public ExecutionStatus(String orderId, String status, String dateTime, GeoData geoData) {
        this.orderId = orderId;
        this.status = status;
        this.dateTime = dateTime;
        this.geoData = geoData;
    }

    public ExecutionStatus() { }

    @XmlElement(name = "OrderID")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @XmlElement(name = "Status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlElement(name = "DateTime")
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @XmlElement(name = "GeoData")
    public GeoData getGeoData() {
        return geoData;
    }

    public void setGeoData(GeoData geoData) {
        this.geoData = geoData;
    }

}
