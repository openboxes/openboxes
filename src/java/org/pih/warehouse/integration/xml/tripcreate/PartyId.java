package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.PartyIdBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "name"})
public class PartyId extends PartyIdBase {

    public PartyId() {
        super();
    }

    public PartyId(String id, String name) {
        super(id, name);
    }
}
