package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class CargoDetails {

    ArrayList <ItemDetails> items;

    public CargoDetails(ArrayList<ItemDetails> items) {
        this.items = items;
    }

    public CargoDetails() {
    }

    @XmlElement(name = "Items")
    public ArrayList<ItemDetails> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemDetails> items) {
        this.items = items;
    }
}
