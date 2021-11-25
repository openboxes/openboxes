package org.pih.warehouse.integration.xml.order;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"cargoType", "stackable", "grounded", "splittable", "dangerousGoodsFlag", "description",
        "quantity", "handlingUnit", "length", "width", "height", "weight", "actualVolume", "actualWeight",
        "volumetricWeight", "ldm"})
public class ItemDetails {
    private String cargoType;
    private String stackable;
    private String grounded;
    private String splittable;
    private String dangerousGoodsFlag;
    private String description;
    private String handlingUnit;
    private String quantity;
    private UnitTypeLength length;
    private UnitTypeLength width;
    private UnitTypeLength height;
    private UnitTypeWeight weight;
    private UnitTypeVolume actualVolume;
    private UnitTypeWeight actualWeight;
    private UnitTypeWeight volumetricWeight;
    private String ldm;

    public ItemDetails(String cargoType, String stackable, String grounded, String splittable, String dangerousGoodsFlag, String description, String handlingUnit, String quantity, UnitTypeLength length, UnitTypeLength width, UnitTypeLength height, UnitTypeWeight weight, UnitTypeVolume actualVolume, UnitTypeWeight actualWeight, UnitTypeWeight volumetricWeight, String ldm) {
        this.cargoType = cargoType;
        this.stackable = stackable;
        this.grounded = grounded;
        this.splittable = splittable;
        this.dangerousGoodsFlag = dangerousGoodsFlag;
        this.description = description;
        this.handlingUnit = handlingUnit;
        this.quantity = quantity;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.actualVolume = actualVolume;
        this.actualWeight = actualWeight;
        this.volumetricWeight = volumetricWeight;
        this.ldm = ldm;
    }

    public ItemDetails() {
    }

    @XmlElement(name = "CargoType")
    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    @XmlElement(name = "Stackable")
    public String getStackable() {
        return stackable;
    }

    public void setStackable(String stackable) {
        this.stackable = stackable;
    }

    @XmlElement(name = "Grounded")
    public String getGrounded() {
        return grounded;
    }

    public void setGrounded(String grounded) {
        this.grounded = grounded;
    }

    @XmlElement(name = "Splittable")
    public String getSplittable() {
        return splittable;
    }

    public void setSplittable(String splittable) {
        this.splittable = splittable;
    }

    @XmlElement(name = "DangerousGoodsFlag")
    public String getDangerousGoodsFlag() {
        return dangerousGoodsFlag;
    }

    public void setDangerousGoodsFlag(String dangerousGoodsFlag) {
        this.dangerousGoodsFlag = dangerousGoodsFlag;
    }
    @XmlElement(name = "CargoDescription")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @XmlElement(name = "HandlingUnit")
    public String getHandlingUnit() {
        return handlingUnit;
    }

    public void setHandlingUnit(String handlingUnit) {
        this.handlingUnit = handlingUnit;
    }
    @XmlElement(name = "Quantity")
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    @XmlElement(name = "Length")
    public UnitTypeLength getLength() {
        return length;
    }

    public void setLength(UnitTypeLength length) {
        this.length = length;
    }
    @XmlElement(name = "Width")
    public UnitTypeLength getWidth() {
        return width;
    }

    public void setWidth(UnitTypeLength width) {
        this.width = width;
    }
    @XmlElement(name = "Height")
    public UnitTypeLength getHeight() {
        return height;
    }

    public void setHeight(UnitTypeLength height) {
        this.height = height;
    }
    @XmlElement(name = "Weight")
    public UnitTypeWeight getWeight() {
        return weight;
    }

    public void setWeight(UnitTypeWeight weight) {
        this.weight = weight;
    }
    @XmlElement(name = "ActualVolume")
    public UnitTypeVolume getActualVolume() {
        return actualVolume;
    }

    public void setActualVolume(UnitTypeVolume actualVolume) {
        this.actualVolume = actualVolume;
    }
    @XmlElement(name = "ActualWeight")
    public UnitTypeWeight getActualWeight() {
        return actualWeight;
    }

    public void setActualWeight(UnitTypeWeight actualWeight) {
        this.actualWeight = actualWeight;
    }
    @XmlElement(name = "VolumetricWeight")
    public UnitTypeWeight getVolumetricWeight() {
        return volumetricWeight;
    }

    public void setVolumetricWeight(UnitTypeWeight volumetricWeight) {
        this.volumetricWeight = volumetricWeight;
    }
    @XmlElement(name = "LDM")
    public String getLdm() {
        return ldm;
    }

    public void setLdm(String ldm) {
        this.ldm = ldm;
    }
}
