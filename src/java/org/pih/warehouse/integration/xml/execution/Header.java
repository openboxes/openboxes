package org.pih.warehouse.integration.xml.execution;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Header")
@XmlType(propOrder={"version","username","password","sequenceNumber","sourceApp","destinationApp"})
public class Header {

    private String version;
    private String username;
    private String password;
    private String sequenceNumber;
    private String sourceApp;
    private String destinationApp;

    public Header() {

    }

    public Header(String version, String sequenceNumber, String sourceApp) {
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
    public String getSourceApp() {
        return sourceApp;
    }

    public void setSourceApp(String sourceApp) {
        this.sourceApp = sourceApp;
    }

    @XmlElement(name = "DestinationApp")
    String getDestinationApp() {
        return destinationApp;
    }

    void setDestinationApp(String destinationApp) {
        this.destinationApp = destinationApp;
    }
}
