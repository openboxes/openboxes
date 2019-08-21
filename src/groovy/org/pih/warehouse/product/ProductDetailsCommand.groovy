/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

class ProductDetailsCommand implements Serializable {

    // Google properties
    String id
    String author
    String googleId
    Date published
    Date updated
    String title
    String description
    String content
    String unitOfMeasure
    Integer packageSize = 1
    String packageUnit
    String packageName
    Date creationTime
    Date modificationTime
    String country
    String language
    String link
    String gtin
    String brand
    String condition

    // NDC properties
    String productType
    String packageDescription
    String ndcCode
    String productNdcCode
    String labelerName
    String strengthNumber
    String strengthUnit
    String pharmClasses
    String dosageForm
    String route
    String proprietaryName
    String nonProprietaryName


    Category category

    Map links = new HashMap()
    List gtins = new ArrayList()
    List images = new ArrayList()


    static constraints = {
        id(nullable: true)
        author(nullable: true)
        googleId(nullable: true)
        published(nullable: true)
        updated(nullable: true)
        title(nullable: true)
        description(nullable: true)
        content(nullable: true)
        unitOfMeasure(nullable: true)
        packageSize(nullable: true)
        packageUnit(nullable: true)
        packageName(nullable: true)
        creationTime(nullable: true)
        modificationTime(nullable: true)
        country(nullable: true)
        language(nullable: true)
        gtin(nullable: true)
        link(nullable: true)
        brand(nullable: true)
        condition(nullable: true)
        category(nullable: true)


        productType(nullable: true)
        packageDescription(nullable: true)
        ndcCode(nullable: true)
        productNdcCode(nullable: true)
        labelerName(nullable: true)
        strengthNumber(nullable: true)
        strengthUnit(nullable: true)
        pharmClasses(nullable: true)
        dosageForm(nullable: true)
        route(nullable: true)
        proprietaryName(nullable: true)
        nonProprietaryName(nullable: true)

    }

    String getStrength() {
        return "${strengthNumber} ${strengthUnit}"
    }

    String getDrugName() {
        return "${nonProprietaryName}, ${route}, ${dosageForm}"
    }
}
