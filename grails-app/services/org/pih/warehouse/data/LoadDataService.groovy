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


import org.grails.plugins.csv.CSVMapReader
import org.pih.warehouse.core.*
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.ProductCatalog

class LoadDataService {

    def locationDataService
    def productService
    def productSupplierDataService

    def importLocations(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        ImportDataCommand command = new ImportDataCommand();
        command.setData(csvReader.readAll());

        locationDataService.validateData(command);
        locationDataService.importData(command);

        csvReader.close();
    }


    def importLocationGroups(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine { Map attr ->
            new LocationGroup(name: attr.get("Name")).save()
        }

        csvReader.close();
    }

    def importOrganizations(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine({ Map attr ->
            String organizationName = attr.get("Organization")
            String partyRole = attr.get("Party Role")

            Organization organization = new Organization(
                    name: organizationName,
                    code: partyRole.substring(0, 3), // FIXME: Should code be provided or generated?
                    partyType: PartyType.findByCode("ORG") // FIXME: Should party type be provided?
            )

            PartyRole role = PartyRole.findAllByRoleType(
                    RoleType.valueOf(partyRole.toUpperCase())
            ).first();

            organization.addToRoles(role);

            organization.save()
        })

        csvReader.close();
    }

    def importCategories(URL csvURL) {
        InputStream csvStream = csvURL.newInputStream();
        String csv = new String(csvStream.getBytes())

        productService.importCategoryCsv(csv)
        csvStream.close();
    }

    def importProducts(URL csvURL) {
        InputStream csvStream = csvURL.newInputStream()
        String csv = new String(csvStream.getBytes())

        def products = productService.validateProducts(csv)
        productService.importProducts(products)
        csvStream.close()
    }

    def importProductCatalog(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine({ Map attr ->
            String id = attr.id;
            String code = attr.code;
            String name = attr.name;
            String description = attr.description;

            ProductCatalog catalog = ProductCatalog.findById(id);

            if (catalog == null) {
                catalog = ProductCatalog.findByCode(code);

                if (catalog != null) {
                    throw new IllegalArgumentException("Duplicate code: " + code);
                }

                catalog = new ProductCatalog(
                        code: code,
                        name: name,
                        description: description
                )
            } else {
                catalog.code = code;
                catalog.name = name;
                catalog.description = description;
            }

            catalog.save()
        })

        csvReader.close();
    }

    def importProductCatalogItems(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys()

        csvReader.eachLine { Map attr ->
            productService.createOrUpdateProductCatalogItem(attr).save()
        }

        csvReader.close();
    }

    def importProductSuppliers(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys()

        csvReader.initFieldKeys();
        List<Map<String, String>> csvItems = csvReader.readAll();

        for (int i = 0; i < csvItems.size(); i++) {
            Map<String, String> currentItem = csvItems.get(i);
            Map<String, String> newItem = new HashMap<String, String>();

            newItem.put("id", currentItem.get("ID"));
            newItem.put("code", currentItem.get("Product Source Code"));
            newItem.put("name", currentItem.get("Product Source Name"));
            newItem.put("productCode", currentItem.get("Product Code"));
            newItem.put("legacyProductCode", currentItem.get("Legacy Product Code"));
            newItem.put("supplierName", currentItem.get("Supplier Name"));
            newItem.put("supplierCode", currentItem.get("Supplier Item No"));
            newItem.put("manufacturerName", currentItem.get("Manufacturer Name"));
            newItem.put("manufacturerCode", currentItem.get("Manufacturer Item No"));
            newItem.put("minOrderQuantity", currentItem.get("Minimum Order Quantity"));
            newItem.put("contractPricePrice", currentItem.get("Contract Price (Each)"));
            newItem.put("contractPriceValidUntil", currentItem.get("Contract Price Valid Until"));
            // FIXME: Rating type is not passing validation
            String ratingType = currentItem.get("Rating Type");
            ratingType = ratingType == "" ? null : ratingType;
            newItem.put("ratingTypeCode", ratingType);

            newItem.put("globalPreferenceTypeName", currentItem.get("Default Global Preference Type"));
            newItem.put("globalPreferenceTypeValidityStartDate", currentItem.get("Preference Type Validity Start Date"));
            newItem.put("globalPreferenceTypeValidityEndDate", currentItem.get("Preference Type Validity End Date"));
            newItem.put("globalPreferenceTypeComments", currentItem.get("Preference Type Comment"));
            newItem.put("defaultProductPackageUomCode", currentItem.get("Default Package Type"));
            newItem.put("defaultProductPackageQuantity", currentItem.get("Quantity per package"));
            newItem.put("defaultProductPackagePrice", currentItem.get("Package price"));

            csvItems.set(i, newItem);
        }

        ImportDataCommand command = new ImportDataCommand();

        command.setData(csvItems);

        productSupplierDataService.validate(command)
        productSupplierDataService.process(command)

        csvReader.close();
    }

}
