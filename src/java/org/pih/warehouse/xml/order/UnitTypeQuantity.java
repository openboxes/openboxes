package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;

public class UnitTypeQuantity {
    private String value;

    public UnitTypeQuantity(String value) {
        this.value = value;
    }

    public UnitTypeQuantity() {
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
