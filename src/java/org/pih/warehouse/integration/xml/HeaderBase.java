package org.pih.warehouse.integration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class HeaderBase {

    private String version;
    private String username;
    private String password;
    private String sequenceNumber;
    private String destinationApp;
    private String sourceApp;

    public HeaderBase() {    }

    public HeaderBase(String version, String username, String password, String sequenceNumber, String destinationApp) {
        this.version = version;
        this.username = username;
        this.password = password;
        this.sequenceNumber = sequenceNumber;
        this.destinationApp = destinationApp;
    }

    public HeaderBase(String version, String sequenceNumber, String sourceApp) {
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

    @XmlElement(name = "DestinationApp")
    String getDestinationApp() {
        return destinationApp;
    }

    void setDestinationApp(String destinationApp) {
        this.destinationApp = destinationApp;
    }

    @XmlElement(name = "SourceApp")
    public String getSourceApp() {
        return sourceApp;
    }

    public void setSourceApp(String sourceApp) {
        this.sourceApp = sourceApp;
    }
}
