package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"value","uom"})
public class UnitTypeWeight {

    private String value;
    private String uom;

    public UnitTypeWeight(String value, String uom) {
        this.value = value;
        this.uom = uom;
    }

    public UnitTypeWeight() {
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "UOM")
    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }
}
