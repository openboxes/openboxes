package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;

public class Remark {
    private String value;

    public Remark(String value) {
        this.value = value;
    }

    public Remark() {
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
