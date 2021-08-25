package org.pih.warehouse.integration.xml.acceptancestatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "AcceptanceStatus")
@XmlType(propOrder = {"header", "action", "tripDetails", "tripOrderDetails"})
public class AcceptanceStatus {
    public AcceptanceStatus() {}
    public AcceptanceStatus(Header header, String action, TripDetails tripDetails, TripOrderDetails tripOrderDetails) {
        this.header = header;
        this.action = action;
        this.tripDetails = tripDetails;
        this.tripOrderDetails = tripOrderDetails;
    }
    private Header header;
    private String action;
    private TripDetails tripDetails;
    private TripOrderDetails tripOrderDetails;
    public void setHeader(Header header) {
        this.header = header;
    }
    @XmlElement(name = "Header")
    public Header getHeader() {
        return header;
    }
    @XmlElement(name = "Action")
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    @XmlElement(name = "TripDetails")
    public TripDetails getTripDetails() {
        return tripDetails;
    }
    public void setTripDetails(TripDetails tripDetails) {
        this.tripDetails = tripDetails;
    }
    @XmlElement(name = "TripOrderDetails")
    public TripOrderDetails getTripOrderDetails() {
        return tripOrderDetails;
    }
    public void setTripOrderDetails(TripOrderDetails tripOrderDetails) {
        this.tripOrderDetails = tripOrderDetails;
    }
}
