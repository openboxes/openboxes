package org.pih.warehouse.integration.xml.tripcreate;

import org.pih.warehouse.integration.xml.AddressBase;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"name", "street","city","state","postal","country","timeZone"} )
public class Address extends AddressBase {
    public Address() {
        super();
    }
    public Address(String name, String street, String city, String state, String postal, String country, String timeZone) {
        super(name, street, city,state, postal, country,timeZone);
    }
}
