package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"firstName","lastName","phone","emailAddress"})
public class ContactData {
    private String firstName;
    private String lastName;
    private Phone phone;
    private String emailAddress;

    public ContactData(String firstName, String lastName, Phone phone, String emailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.emailAddress = emailAddress;
    }

    public ContactData() { }

    @XmlElement(name = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @XmlElement(name = "LastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlElement(name = "Phone")
    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    @XmlElement(name = "EmailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
