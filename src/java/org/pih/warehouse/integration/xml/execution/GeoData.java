package org.pih.warehouse.integration.xml.execution;

import javax.xml.bind.annotation.XmlElement;

public class GeoData {

    private Float latitude;
    private Float longitude;

    public GeoData(Float latitude, Float longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeoData() { }

    @XmlElement(name = "latitude")
    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @XmlElement(name = "longitude")
    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
}
