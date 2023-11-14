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

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Person
import org.pih.warehouse.data.PersonService
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class PersonImportDataService implements ImportDataService {
    PersonService personService

    @Override
    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Person person = personService.createOrUpdatePerson(params)
            if (!person.validate()) {
                person.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1} name = ${person.name}: ${error.getFieldError()}")
                }
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Person person = personService.createOrUpdatePerson(params)
            if (person.validate()) {
                person.save(failOnError: true)
            }
        }
    }
}
