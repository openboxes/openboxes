package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;

public class RefType {

    private String code;
    private String value;

    public RefType(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public RefType() {
    }

    @XmlElement(name = "Code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
