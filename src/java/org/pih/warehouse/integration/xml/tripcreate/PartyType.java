package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"partyID", "type", "contactData"})
public class PartyType {

    private PartyId partyID;
    private String type;
    private ContactData contactData;

    public PartyType(PartyId partyID, String type, ContactData contactData) {
        this.partyID = partyID;
        this.type = type;
        this.contactData = contactData;
    }

    public PartyType() {
    }

    @XmlElement(name = "PartyID")
    public PartyId getPartyID() {
        return partyID;
    }

    public void setPartyID(PartyId partyID) {
        this.partyID = partyID;
    }

    @XmlElement(name = "Type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "ContactData")
    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }
}
