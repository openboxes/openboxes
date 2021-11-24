package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "KNOrgDetails")
@XmlType(propOrder = {"companyCode", "branchCode", "departmentCode", "logicalReceiver", "physicalReceiver", "logicalSender", "physicalSender"})
public class KNOrgDetails {
    private String companyCode;
    private String branchCode;
    private String departmentCode;
    private String physicalReceiver;
    private String physicalSender;
    private String logicalReceiver;
    private String logicalSender;

    public KNOrgDetails() { }

    public KNOrgDetails(String companyCode, String branchCode) {
        this.companyCode = companyCode;
        this.branchCode = branchCode;
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

    @XmlElement(name = "DepartmentCode")
    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    @XmlElement(name = "PhysicalReceiver")
    public String getPhysicalReceiver() {
        return physicalReceiver;
    }

    public void setPhysicalReceiver(String physicalReceiver) {
        this.physicalReceiver = physicalReceiver;
    }


    @XmlElement(name = "PhysicalSender")
    public String getPhysicalSender() {
        return physicalSender;
    }

    public void setPhysicalSender(String physicalSender) {
        this.physicalSender = physicalSender;
    }

    @XmlElement(name = "LogicalReceiver")
    public String getLogicalReceiver() {
        return logicalReceiver;
    }

    public void setLogicalReceiver(String logicalReceiver) {
        this.logicalReceiver = logicalReceiver;
    }

    @XmlElement(name = "LogicalSender")
    public String getLogicalSender() {
        return logicalSender;
    }

    public void setLogicalSender(String logicalSender) {
        this.logicalSender = logicalSender;
    }
}
