package org.pih.warehouse.integration.xml;

import org.pih.warehouse.integration.xml.order.FreightName;

import javax.xml.bind.annotation.*;

@XmlTransient
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"incoterm", "freightName"})
public class TermsOfTradeBase {

    private String incoterm;
    private FreightName freightName;

    public TermsOfTradeBase() { }

    public TermsOfTradeBase(String incoterm, FreightName freightName) {
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
