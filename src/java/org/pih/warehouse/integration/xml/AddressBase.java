package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlTransient
public class AddressBase {

    private String name;
    private String street;
    private String city;
    private String state;
    private String postal;
    private String country;
    private String timeZone;

    public AddressBase(String name, String street, String city, String state, String postal, String country, String timeZone) {
        this.name = name;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postal = postal;
        this.country = country;
        this.timeZone = timeZone;
    }

    public AddressBase() {
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Street")
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @XmlElement(name = "City")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @XmlElement(name = "State")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement(name = "Postal")
    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    @XmlElement(name = "Country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @XmlElement(name = "Timezone")
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
