package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.HeaderBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"version","username","password","sequenceNumber", "sourceApp", "destinationApp"})
public class Header extends HeaderBase {

    public Header() {    }

    public Header(String version, String username, String password, String sequenceNumber, String sourceApp) {
        super (version, username, password, sequenceNumber, sourceApp);
    }

    public Header(String version, String sequenceNumber, String sourceApp) {
        super(version, sequenceNumber, sourceApp);
    }

}
