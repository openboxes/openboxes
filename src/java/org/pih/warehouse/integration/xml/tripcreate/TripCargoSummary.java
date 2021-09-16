package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"totalQuantity", "totalVolume","totalWeight"})
public class TripCargoSummary {

    private UnitTypeQuantity totalQuantity;
    private UnitTypeVolume totalVolume;
    private UnitTypeWeight totalWeight;

    public TripCargoSummary(UnitTypeQuantity totalQuantity, UnitTypeVolume totalVolume, UnitTypeWeight totalWeight) {
        this.totalQuantity = totalQuantity;
        this.totalVolume = totalVolume;
        this.totalWeight = totalWeight;
    }

    public TripCargoSummary() {
    }

    @XmlElement(name = "TotalQuantity")
    public UnitTypeQuantity getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(UnitTypeQuantity totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @XmlElement(name = "TotalVolume")
    public UnitTypeVolume getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(UnitTypeVolume totalVolume) {
        this.totalVolume = totalVolume;
    }

    @XmlElement(name = "TotalWeight")
    public UnitTypeWeight getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(UnitTypeWeight totalWeight) {
        this.totalWeight = totalWeight;
    }
}
