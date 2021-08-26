package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"incoterm", "freightName"})
public class TermsOfTrade {

    private String incoterm;
    private FreightName freightName;

    public TermsOfTrade() { }

    public TermsOfTrade( String incoterm, FreightName freightName) {
        this.incoterm = incoterm;
        this.freightName = freightName;
    }

    @XmlElement(name = "Incoterm")
    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    @XmlElement(name = "FreightName")
    public FreightName getFreightName() {
        return freightName;
    }

    public void setFreightName(FreightName freightName) {
        this.freightName = freightName;
    }
}
