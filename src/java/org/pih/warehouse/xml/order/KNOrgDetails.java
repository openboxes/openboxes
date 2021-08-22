package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "KNOrgDetails")
@XmlType(propOrder = {"companyCode", "branchCode", "departmentCode"})
public class KNOrgDetails {
    private String companyCode;
    private String branchCode;
    private String departmentCode;

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
}
