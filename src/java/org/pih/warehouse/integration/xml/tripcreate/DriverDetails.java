package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"idProof","address","contactData"})
public class DriverDetails {
    private IdProof idProof;
    private Address address;
    private ContactData contactData;

    @XmlElement(name = "IDProof")
    public IdProof getIdProof() {
        return idProof;
    }

    public void setIdProof(IdProof idProof) {
        this.idProof = idProof;
    }

    @XmlElement(name = "Address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @XmlElement(name = "ContactData")
    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }
}
