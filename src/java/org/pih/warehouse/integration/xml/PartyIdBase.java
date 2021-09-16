package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class PartyIdBase {
    private String id;
    private String name;

    public PartyIdBase(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PartyIdBase() {
    }

    @XmlElement(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
