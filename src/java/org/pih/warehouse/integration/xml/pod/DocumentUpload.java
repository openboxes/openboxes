package org.pih.warehouse.integration.xml.pod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="DocumentUpload")
@XmlType(propOrder = {"header", "action", "documentType", "uploadDetails"})
public class DocumentUpload {
    private Header header;
    private String action;
    private String documentType;
    private UploadDetails uploadDetails;

    @XmlElement(name = "Header")
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    @XmlElement(name = "Action")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @XmlElement(name = "DocumentType")
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @XmlElement(name = "UploadDetails")
    public UploadDetails getUploadDetails() {
        return uploadDetails;
    }

    public void setUploadDetails(UploadDetails uploadDetails) {
        this.uploadDetails = uploadDetails;
    }
}
