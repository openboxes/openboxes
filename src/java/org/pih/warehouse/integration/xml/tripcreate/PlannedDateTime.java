package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.PlannedDateTimeBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"from","to"})
public class PlannedDateTime extends PlannedDateTimeBase {
    public PlannedDateTime() {
        super();
    }
    public PlannedDateTime(String from, String to) {
        super(from, to);
    }
}
