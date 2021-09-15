package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class PhoneBase {

    private String countryCode;
    private String contactNo;

    public PhoneBase(String countryCode, String contactNo) {
        this.countryCode = countryCode;
        this.contactNo = contactNo;
    }

    public PhoneBase() {}

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
