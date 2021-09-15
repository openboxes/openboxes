package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class FreightNameBase {

    private String term;
    private String name;

    public FreightNameBase() { }

    public FreightNameBase(String term, String name) {
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
