package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlType(propOrder = {"cargoType", "stackable", "grounded", "splittable", "dangerousGoodsFlag", "description",
        "handlingUnit", "quantity", "length", "width", "height", "weight", "actualVolume", "actualWeight",
        "volumetricWeight", "ldm"})
public class ItemDetails {
    private String cargoType;
    private String stackable;
    private String grounded;
    private String splittable;
    private String dangerousGoodsFlag;
    private String description;
    private String handlingUnit;
    private Integer quantity;
    private DimensionProperties length;
    private DimensionProperties width;
    private DimensionProperties height;
    private UnitTypeWeight weight;
    private UnitTypeVolume actualVolume;
    private UnitTypeWeight actualWeight;
    private UnitTypeWeight volumetricWeight;
    private BigDecimal ldm;

    public ItemDetails(String cargoType, String stackable, String grounded, String splittable, String dangerousGoodsFlag, String description, String handlingUnit, Integer quantity, DimensionProperties length, DimensionProperties width, DimensionProperties height, UnitTypeWeight weight, UnitTypeVolume actualVolume, UnitTypeWeight actualWeight, UnitTypeWeight volumetricWeight, BigDecimal ldm) {
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
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    @XmlElement(name = "Length")
    public DimensionProperties getLength() {
        return length;
    }

    public void setLength(DimensionProperties length) {
        this.length = length;
    }
    @XmlElement(name = "Width")
    public DimensionProperties getWidth() {
        return width;
    }

    public void setWidth(DimensionProperties width) {
        this.width = width;
    }
    @XmlElement(name = "Height")
    public DimensionProperties getHeight() {
        return height;
    }

    public void setHeight(DimensionProperties height) {
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
    public BigDecimal getLdm() {
        return ldm;
    }

    public void setLdm(BigDecimal ldm) {
        this.ldm = ldm;
    }
}
