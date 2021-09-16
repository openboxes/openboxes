package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Trip")
@XmlType(propOrder = {"header", "action", "tripId", "externalTripId","knOrgDetails","tripDetails","tripOrderDetails"})
public class TripCreate {
    private Header header;
    private String action;
    private String tripId;
    private String externalTripId;
    private KNOrgDetails knOrgDetails;
    private TripDetails tripDetails;
    private TripOrderDetails tripOrderDetails;

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

    @XmlElement(name = "TripID")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @XmlElement(name = "ExternalTripID")
    public String getExternalTripId() {
        return externalTripId;
    }

    public void setExternalTripId(String externalTripId) {
        this.externalTripId = externalTripId;
    }

    @XmlElement(name = "KNOrgDetails")
    public KNOrgDetails getKnOrgDetails() {
        return knOrgDetails;
    }

    public void setKnOrgDetails(KNOrgDetails knOrgDetails) {
        this.knOrgDetails = knOrgDetails;
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
