package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.FreightNameBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"term", "name"})
public class FreightName extends FreightNameBase {
    public FreightName() {
        super();
    }

    public FreightName(String term, String name) {
        super (term, name);
    }
}
