package org.pih.warehouse.integration.xml.executionstatus;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlType (name="",propOrder={"header","tripID","executionStatus"})
@XmlRootElement(name = "execution")
public class TripExecution {

    private String tripID;
    private Header header;
    private List<ExecutionStatus> executionStatus = null;

    public TripExecution() {}
    public TripExecution(String tripId, Header header, List <ExecutionStatus> executionStatus) {
        this.tripID = tripId;
        this.header = header;
        this.executionStatus = executionStatus;
    }

    @XmlElement(name = "TripID")
    public String getTripID() {
        return tripID;
    }

    void setTripID(String tripID) {
        this.tripID = tripID;
    }

    @XmlElement(name = "Header")
    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }

    public List <ExecutionStatus> getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(List <ExecutionStatus> executionStatuses) {
        this.executionStatus = executionStatuses;
    }

}

