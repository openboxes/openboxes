package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.GoodsValueBase;

import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlType(propOrder = {"value","currency"})
public class GoodsValue extends GoodsValueBase {
    public GoodsValue() {
        super();
    }
    public GoodsValue(BigDecimal value, String currency){
        super(value, currency);
    }
}
