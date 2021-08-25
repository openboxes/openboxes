package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

@XmlType(propOrder = {"partyTypes"})
public class OrderParties {

    public OrderParties() { }

    public OrderParties(ArrayList<PartyType> partyTypes) {
        this.partyTypes = partyTypes;
    }

    private ArrayList <PartyType> partyTypes;

    @XmlElement(name = "PartyType")
    public ArrayList<PartyType> getPartyTypes() {
        return partyTypes;
    }

    public void setPartyTypes(ArrayList<PartyType> partyTypes) {
        this.partyTypes = partyTypes;
    }
}
