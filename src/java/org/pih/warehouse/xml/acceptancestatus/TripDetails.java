package org.pih.warehouse.xml.acceptancestatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "TripDetails")
@XmlType(propOrder = {"tripId", "carrier"})
public class TripDetails {
    private String tripId;
    private Carrier carrier;

    @XmlElement(name = "TripID")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @XmlElement(name = "Carrier")
    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }
}
