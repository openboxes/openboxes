package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"countryCode", "contactNo"})
public class Phone {

    private String countryCode;
    private String contactNo;

    public Phone(String countryCode, String contactNo) {
        this.countryCode = countryCode;
        this.contactNo = contactNo;
    }

    public Phone() {}

    @XmlElement(name = "CountryCode")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @XmlElement(name = "ContactNo")
    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
}
