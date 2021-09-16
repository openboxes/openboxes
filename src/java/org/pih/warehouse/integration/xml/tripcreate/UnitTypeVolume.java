package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlType(propOrder = {"value","uom"})
public class UnitTypeVolume {

    private BigDecimal value;
    private String uom;

    public UnitTypeVolume(BigDecimal value, String uom) {
        this.value = value;
        this.uom = uom;
    }

    public UnitTypeVolume() {
    }

    @XmlElement(name = "Value")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
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
