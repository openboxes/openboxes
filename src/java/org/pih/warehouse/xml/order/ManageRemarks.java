package org.pih.warehouse.xml.order;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class ManageRemarks {
    private ArrayList<Remark> remarks;

    public ManageRemarks(ArrayList<Remark> remarks) {
        this.remarks = remarks;
    }

    public ManageRemarks() {
    }

    @XmlElement(name = "Remark")
    public ArrayList<Remark> getRemarks() {
        return remarks;
    }

    public void setRemarks(ArrayList<Remark> remarks) {
        this.remarks = remarks;
    }
}
