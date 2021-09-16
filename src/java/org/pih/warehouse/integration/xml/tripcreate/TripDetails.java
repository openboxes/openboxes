package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"tripType", "carrier", "carrierInstructions",
        "additionalConditions","temperatureRegime","timeForLoadingAndPenalty",
        "vehicleDetails","driverDetails","tripCargoSummary"})
public class TripDetails {
    private String tripType;
    private Carrier carrier;
    private String carrierInstructions;
    private String additionalConditions;
    private String temperatureRegime;
    private String timeForLoadingAndPenalty;
    private VehicleDetails vehicleDetails;
    private DriverDetails driverDetails;
    private TripCargoSummary tripCargoSummary;

    @XmlElement(name = "TripType")
    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    @XmlElement(name = "Carrier")
    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    @XmlElement(name = "CarrierInstructions")
    public String getCarrierInstructions() {
        return carrierInstructions;
    }

    public void setCarrierInstructions(String carrierInstructions) {
        this.carrierInstructions = carrierInstructions;
    }

    @XmlElement(name = "AdditionalConditions")
    public String getAdditionalConditions() {
        return additionalConditions;
    }

    public void setAdditionalConditions(String additionalConditions) {
        this.additionalConditions = additionalConditions;
    }

    @XmlElement(name = "TemperatureRegime")
    public String getTemperatureRegime() {
        return temperatureRegime;
    }

    public void setTemperatureRegime(String temperatureRegime) {
        this.temperatureRegime = temperatureRegime;
    }

    @XmlElement(name = "TimeForLoadingAndPenalty")
    public String getTimeForLoadingAndPenalty() {
        return timeForLoadingAndPenalty;
    }

    public void setTimeForLoadingAndPenalty(String timeForLoadingAndPenalty) {
        this.timeForLoadingAndPenalty = timeForLoadingAndPenalty;
    }

    @XmlElement(name = "VehicleDetails")
    public VehicleDetails getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(VehicleDetails vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    @XmlElement(name = "DriverDetails")
    public DriverDetails getDriverDetails() {
        return driverDetails;
    }

    public void setDriverDetails(DriverDetails driverDetails) {
        this.driverDetails = driverDetails;
    }

    @XmlElement(name = "TripCargoSummary")
    public TripCargoSummary getTripCargoSummary() {
        return tripCargoSummary;
    }

    public void setTripCargoSummary(TripCargoSummary tripCargoSummary) {
        this.tripCargoSummary = tripCargoSummary;
    }
}
