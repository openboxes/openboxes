package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class PlannedDateTimeBase {

    private String from;
    private String to;

    public PlannedDateTimeBase(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public PlannedDateTimeBase() {
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
