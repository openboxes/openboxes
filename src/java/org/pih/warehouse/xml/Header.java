package org.pih.warehouse.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Header")
@XmlType(propOrder={"version","username","password","sequenceNumber","destinationApp"})
public class Header {

    private String version;
    private String username;
    private String password;
    private String sequenceNumber;
    private String destinationApp;

    public Header() {

    }

    public Header(String version, String username, String password, String sequenceNumber, String destinationApp) {
        this.version = version;
        this.username = username;
        this.password = password;
        this.sequenceNumber = sequenceNumber;
        this.destinationApp = destinationApp;
    }

    @XmlElement(name = "version")
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
}