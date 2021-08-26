package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"term", "name"})
public class FreightName {

    private String term;
    private String name;

    public FreightName() { }

    public FreightName (String term, String name) {
        this.term = term;
        this.name = name;
    }

    @XmlElement(name = "Term")
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
