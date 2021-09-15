package org.pih.warehouse.integration.xml.order;

import org.pih.warehouse.integration.xml.TermsOfTradeBase;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"incoterm", "freightName"})
public class TermsOfTrade extends TermsOfTradeBase {

    public TermsOfTrade() {
        super();
    }

    public TermsOfTrade( String incoterm, FreightName freightName) {
        super(incoterm, freightName);
    }
}
