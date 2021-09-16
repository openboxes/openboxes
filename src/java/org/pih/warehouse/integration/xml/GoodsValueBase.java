package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlTransient
@XmlAccessorType(XmlAccessType.PROPERTY)
public class GoodsValueBase {
    private BigDecimal value;
    private String currency;

    public GoodsValueBase() { }

    public GoodsValueBase(BigDecimal value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    @XmlElement(name = "Value")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @XmlElement(name = "Currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
