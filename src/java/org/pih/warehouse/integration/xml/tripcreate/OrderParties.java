package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class OrderParties {

    private ArrayList<PartyType> partyTypes;

    public OrderParties() { }

    public OrderParties(ArrayList<PartyType> partyTypes) {
        this.partyTypes = partyTypes;
    }

    @XmlElement(name = "PartyType")
    public ArrayList<PartyType> getPartyTypes() {
        return partyTypes;
    }

    public void setPartyTypes(ArrayList<PartyType> partyTypes) {
        this.partyTypes = partyTypes;
    }
}
