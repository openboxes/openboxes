package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"vehicleTypeCode", "vehicleModelCode","registrationNumber", "license", "applicableForDangerousGoods", "properties"})
public class VehicleDetails {
    private String vehicleTypeCode;
    private String vehicleModelCode;
    private String registrationNumber;
    private String license;
    private String applicableForDangerousGoods;
    private Properties properties;

    @XmlElement(name = "VehicleTypeCode")
    public String getVehicleTypeCode() {
        return vehicleTypeCode;
    }

    public void setVehicleTypeCode(String vehicleTypeCode) {
        this.vehicleTypeCode = vehicleTypeCode;
    }

    @XmlElement(name = "VehicleModelCode")
    public String getVehicleModelCode() {
        return vehicleModelCode;
    }

    public void setVehicleModelCode(String vehicleModelCode) {
        this.vehicleModelCode = vehicleModelCode;
    }

    @XmlElement(name = "RegistrationNumber")
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @XmlElement(name = "License")
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @XmlElement(name = "ApplicableForDangerousGoods")
    public String getApplicableForDangerousGoods() {
        return applicableForDangerousGoods;
    }

    public void setApplicableForDangerousGoods(String applicableForDangerousGoods) {
        this.applicableForDangerousGoods = applicableForDangerousGoods;
    }

    @XmlElement(name = "Properties")
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
