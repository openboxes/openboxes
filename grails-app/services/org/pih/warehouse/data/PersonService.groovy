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

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Person

@Transactional
class PersonService {

    /**
     * @return The first person we can find who has the given email, giving preference to active people.
     */
    private Person getPersonByEmail(String email) {
        List<Person> people = Person.findAllByEmail(email)
        if (people.empty) {
            return null
        }

        // We give preference to active people, so if there is one found, return it, otherwise return anyone.
        return people.find{ it.active } ?: people.first()
    }

    /**
     * @return The first person we can find who is active and has the given email
     */
    Person getActivePersonByEmail(String email) {
        return Person.findByActiveAndEmail(true, email)
    }

    /**
     * Fetch a user via their full name. We require the combined name to be provided (instead of first name
     * and last name individually) because while internally we split the name so that it conforms to the firstName and
     * lastName fields in the Person table, we want to eventually move away from forcing that name structure upon users.
     *
     * @return Returns the first person we can find who has the given email, giving preference to active people.
     */
    private Person getPersonByName(String combinedNames) {
        String[] names = extractNames(combinedNames)
        List<Person> people = Person.findAllByFirstNameAndLastName(names[0], names[1])
        if (people?.empty) {
            return null
        }

        // We give preference to active people, so if there is one found, return it, otherwise return anyone.
        return people.find{ it.active } ?: people.first()
    }

    /**
     * Fetch an active user via their full name. We require the combined name to be provided (instead of first name
     * and last name individually) because while internally we split the name so that it conforms to the firstName and
     * lastName fields in the Person table, we want to eventually move away from forcing that name structure upon users.
     *
     * @param combinedNames The person's full name (first name + last name) combined into a single String.
     * @return A Person who is active and has a firstName and lastName matching the combinedNames
     */
    Person getActivePersonByName(String combinedNames) {
        if (StringUtils.isBlank(combinedNames)) {
            return null
        }

        String[] names = extractNames(combinedNames)
        return Person.findByActiveAndFirstNameAndLastName(true, names[0], names[1])
    }

    /**
     * @param recipient Either a RFC822O internet/email address (ex: "Justin Miranda <justin@openboxes.com>")
     *        or simply the recipient's name (ex: "Justin Miranda"). We accept both formats since either
     *        format can be provided during data imports.
     * @return A Person who has the provided name and email. Will be a new Person if they didn't previously exist.
     */
    Person getOrCreatePersonByRecipient(String recipient) {
        if (StringUtils.isBlank(recipient)) {
            return null
        }

        InternetAddress internetAddress
        try {
            internetAddress = new InternetAddress(recipient, false)
        }
        catch (AddressException ignored) {
            // If recipient isn't a valid internet address, it must be a regular name.
            return getOrCreatePersonFromNames(recipient)
        }

        // If a person exists with the given email, return them. Note that this can return inactive people so
        // the caller must properly handle that case. We do this to avoid creating duplicate users.
        Person person = getPersonByEmail(internetAddress.address)
        if (person) {
            return person
        }

        // Otherwise create a new person.
        if (!internetAddress.personal) {
            throw new RuntimeException("Cannot save new recipient without a name: ${recipient}")
        }
        String[] names = extractNames(internetAddress.personal)
        person = new Person(firstName: names[0], lastName: names[1], email: internetAddress.address)
        if (!person.save(flush: true)) {
            throw new ValidationException("Cannot save recipient ${recipient} due to errors", person.errors)
        }
        return person
    }

    /**
     * @param combinedNames The person's full name (first name + last name) combined into a single String.
     * @return A Person who has the provided combinedName. Will be a new Person if they didn't previously exist.
     */
    private Person getOrCreatePersonFromNames(String combinedNames) {
        // If a person exists with the given name, return them. Note that this can return inactive people so
        // the caller must properly handle that case. We do this to avoid creating duplicate users.
        Person person = getPersonByName(combinedNames)
        if (person) {
            return person
        }

        String[] names = extractNames(combinedNames)
        person = new Person(firstName: names[0], lastName: names[1])
        if (!person.save(flush: true)) {
            throw new ValidationException("Cannot save recipient ${combinedNames} due to errors", person.errors)
        }
        return person
    }

    private String[] extractNames(String combinedNames) {
        // TODO: We should be smarter about how we process names here. If someone has the name "John Thomas Blanchard",
        //       how do we split it into first name and last name? If someone doesn't have a last name, what do we do?
        //       This highlights the fact that we're trying to make a judgement call about a name (a single first name
        //       and last name pair) that doesn't apply universally across cultures. Better would be to have a single
        //       "name" field that stores the name plainly. Then this whole method can go away.
        String[] names = combinedNames.split(" ", 2)
        if (names.length <= 1) {
            throw new RuntimeException("Recipient ${combinedNames} must have at least two names (i.e. first name and last name)")
        }
        return names
    }
}
