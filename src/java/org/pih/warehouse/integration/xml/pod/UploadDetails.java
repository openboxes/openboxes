package org.pih.warehouse.integration.xml.pod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name = "UploadDetails")
@XmlType(propOrder = {"orderId", "sourceType"})
public class UploadDetails {

    List<String> orderId;
    SourceType sourceType;

    @XmlElement(name="OrderID")
    public List<String> getOrderId() {
        return orderId;
    }

    public void setOrderId(List<String> orderId) {
        this.orderId = orderId;
    }

    @XmlElement(name = "SourceType")
    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
}
