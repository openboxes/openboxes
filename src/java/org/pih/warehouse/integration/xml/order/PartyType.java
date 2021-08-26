package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"partyID", "type", "contactData"})
public class PartyType {

    private PartyID partyID;
    private String type;
    private ContactData contactData;

    @XmlElement(name = "PartyID")
    public PartyID getPartyID() {
        return partyID;
    }

    public void setPartyID(PartyID partyID) {
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
