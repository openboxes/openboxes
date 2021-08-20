package org.pih.warehouse.xml.pod;

import javax.xml.bind.annotation.XmlElement;

public class SourceType {
    private Document document;

    @XmlElement(name = "Document")
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
