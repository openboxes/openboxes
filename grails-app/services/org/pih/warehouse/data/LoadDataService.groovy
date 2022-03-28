/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import au.com.bytecode.opencsv.CSVReader
import org.grails.plugins.csv.CSVReaderUtils
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.importer.LocationCsvImporter

class LoadDataService {

    def locationDataService
    def productService

    def importLocations(URL csvURL) {
        BufferedReader reader = csvURL.newInputStream().newReader();

        LocationCsvImporter importer = new LocationCsvImporter(
                reader,
                locationDataService
        );


        importer.importData()

        reader.close();
    }


    def importLocationGroups(URL csvURL) {
        BufferedReader reader = csvURL.newInputStream().newReader();

        CSVReader csvReader = CSVReaderUtils.toCsvReader(reader, [
                'skipLines': 1
        ]);

        int nameIndex = 0;

        CSVReaderUtils.eachLine(
                csvReader,
                { tokens -> new LocationGroup(name: tokens[nameIndex]).save() }
        )
    }

    def importOrganizations(URL csvURL) {
        BufferedReader reader = csvURL.newInputStream().newReader();

        CSVReader csvReader = CSVReaderUtils.toCsvReader(reader, [
                'skipLines': 1
        ]);

        int organizationIndex = 0;
        int partyRoleIndex = 1;

        CSVReaderUtils.eachLine(
                csvReader,
                { tokens ->
                    Organization organization = new Organization(
                            name: tokens[organizationIndex],
                            code: tokens[organizationIndex].substring(0, 3), // FIXME: Code should be provided or generated?
                            partyType: PartyType.findByCode("ORG") // FIXME: Party type should be provided?
                    )

                    PartyRole role = PartyRole.findAllByRoleType(
                            RoleType.valueOf(tokens[partyRoleIndex].toUpperCase())
                    ).first();

                    organization.addToRoles(role);

                    organization.save()
                }
        )
    }

    def importCategories(URL csvURL) {
        String csv = new String(csvURL.newInputStream().getBytes())

        productService.importCategoryCsv(csv)
    }

    def importProducts(URL csvURL) {
        String csv = new String(csvURL.newInputStream().getBytes())

        def products = productService.validateProducts(csv)
        productService.importProducts(products)
    }

}
