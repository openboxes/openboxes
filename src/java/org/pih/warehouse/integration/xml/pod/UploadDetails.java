package org.pih.warehouse.integration.xml.pod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name = "UploadDetails")
@XmlType(propOrder = {"documentName", "url", "documentFile"})
public class UploadDetails {

    String documentName;
    String url;
    String documentFile;

    @XmlElement(name = "DocumentName")
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    @XmlElement(name = "URL")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "DocumentFile")
    public String getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(String documentFile) {
        this.documentFile = documentFile;
    }
}
