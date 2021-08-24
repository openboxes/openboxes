package org.pih.warehouse.integration.xml.acceptancestatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Header")
@XmlType(propOrder={"version","username","password","sequenceNumber", "sourceApp"})
public class Header {

    private String version;
    private String username;
    private String password;
    private String sequenceNumber;
    private String sourceApp;

    public Header() {

    }

    public Header(String version, String username, String password, String sequenceNumber, String sourceApp) {
        this.version = version;
        this.sequenceNumber = sequenceNumber;
        this.sourceApp = sourceApp;
    }

    @XmlElement(name = "Version")
    String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "Username")
    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name = "Password")
    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "SequenceNumber")
    String getSequenceNumber() {
        return sequenceNumber;
    }

    void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @XmlElement(name = "SourceApp")
    String getSourceApp() {
        return sourceApp;
    }

    void setSourceApp(String sourceApp) {
        this.sourceApp = sourceApp;
    }
}