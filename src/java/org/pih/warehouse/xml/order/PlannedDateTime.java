package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"from","to"})
public class PlannedDateTime {

    private String from;
    private String to;

    public PlannedDateTime(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public PlannedDateTime() {
    }

    @XmlElement(name = "From")
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @XmlElement(name = "To")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
