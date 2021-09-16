package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"weight", "volume", "length", "width", "height"})
public class Properties {

    private WeightProperties weight;
    private VolumeProperties volume;
    private DimensionProperties length;
    private DimensionProperties width;
    private DimensionProperties height;

    public Properties(WeightProperties weight, VolumeProperties volume, DimensionProperties length, DimensionProperties width, DimensionProperties height) {
        this.weight = weight;
        this.volume = volume;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public Properties() {
    }

    @XmlElement(name = "Weight")
    public WeightProperties getWeight() {
        return weight;
    }

    public void setWeight(WeightProperties weight) {
        this.weight = weight;
    }

    @XmlElement(name = "Volume")
    public VolumeProperties getVolume() {
        return volume;
    }

    public void setVolume(VolumeProperties volume) {
        this.volume = volume;
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
}
