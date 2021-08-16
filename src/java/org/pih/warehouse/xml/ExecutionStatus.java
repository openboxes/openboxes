package org.pih.warehouse.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "executionStatus")
@XmlType(propOrder={"orderId","status","dateTime", "geoData"})
public class ExecutionStatus {

    @XmlElement(name = "OrderID")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @XmlElement(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlElement(name = "dateTime")
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @XmlElement(name = "geoData")
    public String getGeoData() {
        return geoData;
    }

    public void setGeoData(String geoData) {
        this.geoData = geoData;
    }

    private String orderId;
    private String status;
    private String dateTime;
    private String geoData;

}
