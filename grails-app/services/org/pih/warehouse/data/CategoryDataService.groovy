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

import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Category
import org.springframework.validation.BeanPropertyBindingResult

class CategoryDataService {

    Boolean validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->

            Category category = createOrUpdateCategory(params)

            if (params.parentCategoryId) {
                Category parentCategory = Category.get(params.parentCategoryId)
                if (!parentCategory) {
                    command.errors.rejectValue("parentCategory", "category.parentCategory.invalid")
                }
            }

            if (!category.validate()) {
                category.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Category ${category.name} is invalid: ${error.getFieldError()}")
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Category category = createOrUpdateCategory(params)
            if (category.validate()) {
                category.save(failOnError: true)
            }
        }

    }

    Category createOrUpdateCategory(Map params) {
        Category category = Category.findByIdOrName(params.id, params.name)
        if (!category) {
            category = new Category()
        }
        category.id = params.id
        category.name = params.name
        if (params.parentCategoryId) {
            category.parentCategory = Category.get(params.parentCategoryId)
        }
        return category
    }
}
