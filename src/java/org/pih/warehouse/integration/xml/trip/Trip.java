package org.pih.warehouse.integration.xml.trip;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Trip")
@XmlType(propOrder = {"tripId", "tripOrderDetails"})
public class Trip {

    public Trip(){}

    public Trip(String tripId, TripOrderDetails tripOrderDetails){
        this.tripId = tripId;
        this.tripOrderDetails = tripOrderDetails;
    }

    private String tripId;

    @XmlElement(name = "TripID")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    private TripOrderDetails tripOrderDetails;

    @XmlElement(name = "TripOrderDetails")
    public TripOrderDetails getTripOrderDetails() {
        return tripOrderDetails;
    }

    public void setTripOrderDetails(TripOrderDetails tripOrderDetails) {
        this.tripOrderDetails = tripOrderDetails;
    }
}
