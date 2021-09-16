package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

public class UnitTypeQuantity {
    private BigDecimal value;

    public UnitTypeQuantity(BigDecimal value) {
        this.value = value;
    }

    public UnitTypeQuantity() {
    }

    @XmlElement(name = "Value")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
