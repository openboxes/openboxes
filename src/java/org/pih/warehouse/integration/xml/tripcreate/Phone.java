package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.PhoneBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"countryCode", "contactNo"})
public class Phone extends PhoneBase {
    public Phone() {
        super();
    }
    public Phone(String countryCode, String contactNo) {
        super(countryCode, contactNo);
    }
}
