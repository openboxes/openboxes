/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.product.Category
import org.springframework.validation.Errors

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

class ImporterUtil {

    static DateFormat EXCEL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

    static getProductService() {
        return ApplicationHolder.getApplication().getMainContext().getBean("productService")
    }

    /**
     *
     * @param categoryName
     * @param errors
     * @return
     */
    static Category findOrCreateCategory(String categoryName, Errors errors) {
        def category = Category.findByName(categoryName)
        if (!category) {
            category = new Category(name: categoryName, parentCategory: getProductService().getRootCategory())
            if (!category.validate()) {
                category.errors.allErrors.each {
                    errors.addError(it)
                }
            } else {
                category.save(failOnError: true)
            }
        }
        return category
    }


    /**
     *
     * @param expirationDate
     * @param errors
     * @return
     */
    static Date parseDate(Object expirationDate, Errors errors) {
        if (expirationDate) {
            // If we're passed a date, we can just set the expiration
            if (expirationDate instanceof org.joda.time.LocalDate) {
                expirationDate = expirationDate.toDateMidnight().toDate()
            } else {
                try {

                    expirationDate = EXCEL_DATE_FORMAT.parse(expirationDate)
                } catch (ParseException e) {
                    errors.reject("Could not parse date " + expirationDate + " " + e.getMessage() + ".  Expected date format: yyyy-MM-dd")
                }
            }
        }

    }
}
