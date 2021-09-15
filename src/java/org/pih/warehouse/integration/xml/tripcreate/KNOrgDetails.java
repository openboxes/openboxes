package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "KNOrgDetails")
@XmlType(propOrder = {"companyCode", "branchCode", "physicalReciever","logicalReceiver", "physicalSender","logicalSender"})
public class KNOrgDetails {
    private String companyCode;
    private String branchCode;
    private String physicalReciever;
    private String logicalReceiver;
    private String physicalSender;
    private String logicalSender;

    public KNOrgDetails() { }

    public KNOrgDetails(String companyCode, String branchCode) {
        this.companyCode = companyCode;
        this.branchCode = branchCode;
    }

    @XmlElement(name = "PhysicalReciever")
    public String getPhysicalReciever() {
        return physicalReciever;
    }

    public void setPhysicalReciever(String physicalReciever) {
        this.physicalReciever = physicalReciever;
    }

    @XmlElement(name = "LogicalReceiver")
    public String getLogicalReceiver() {
        return logicalReceiver;
    }

    public void setLogicalReceiver(String logicalReceiver) {
        this.logicalReceiver = logicalReceiver;
    }

    @XmlElement(name = "PhysicalSender")
    public String getPhysicalSender() {
        return physicalSender;
    }

    public void setPhysicalSender(String physicalSender) {
        this.physicalSender = physicalSender;
    }

    @XmlElement(name = "LogicalSender")
    public String getLogicalSender() {
        return logicalSender;
    }

    public void setLogicalSender(String logicalSender) {
        this.logicalSender = logicalSender;
    }

    @XmlElement(name = "CompanyCode")
    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @XmlElement(name = "BranchCode")
    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
}