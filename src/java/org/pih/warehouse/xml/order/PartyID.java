package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "name"})
public class PartyID {

    private String id;
    private String name;

    public PartyID(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PartyID() { }

    @XmlElement(name="ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name="Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
