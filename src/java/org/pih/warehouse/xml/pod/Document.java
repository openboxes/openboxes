package org.pih.warehouse.xml.pod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"documentName", "documentFile"})
public class Document {

    private String documentName;
    private String documentFile;

    @XmlElement(name="DocumentName")
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    @XmlElement(name="DocumentFile")
    public String getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(String documentFile) {
        this.documentFile = documentFile;
    }
}
