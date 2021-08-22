package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class ManageReferences {

    public ManageReferences(ArrayList<RefType> refTypes) {
        this.refTypes = refTypes;
    }

    public ManageReferences() {
    }

    @XmlElement(name = "RefType")
    public ArrayList<RefType> getRefTypes() {
        return refTypes;
    }

    public void setRefTypes(ArrayList<RefType> refTypes) {
        this.refTypes = refTypes;
    }

    private ArrayList<RefType> refTypes;
}
