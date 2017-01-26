/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory



import grails.test.mixin.*
import spock.lang.*

@TestFor(TransactionEntryController)
@Mock(TransactionEntry)
class TransactionEntryControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.transactionEntryInstanceList
            model.transactionEntryInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.transactionEntryInstance!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'POST'
            def transactionEntry = new TransactionEntry()
            transactionEntry.validate()
            controller.save(transactionEntry)

        then:"The create view is rendered again with the correct model"
            model.transactionEntryInstance!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            transactionEntry = new TransactionEntry(params)

            controller.save(transactionEntry)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/transactionEntry/show/1'
            controller.flash.message != null
            TransactionEntry.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def transactionEntry = new TransactionEntry(params)
            controller.show(transactionEntry)

        then:"A model is populated containing the domain instance"
            model.transactionEntryInstance == transactionEntry
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def transactionEntry = new TransactionEntry(params)
            controller.edit(transactionEntry)

        then:"A model is populated containing the domain instance"
            model.transactionEntryInstance == transactionEntry
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/transactionEntry/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def transactionEntry = new TransactionEntry()
            transactionEntry.validate()
            controller.update(transactionEntry)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.transactionEntryInstance == transactionEntry

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            transactionEntry = new TransactionEntry(params).save(flush: true)
            controller.update(transactionEntry)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/transactionEntry/show/$transactionEntry.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/transactionEntry/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def transactionEntry = new TransactionEntry(params).save(flush: true)

        then:"It exists"
            TransactionEntry.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(transactionEntry)

        then:"The instance is deleted"
            TransactionEntry.count() == 0
            response.redirectedUrl == '/transactionEntry/index'
            flash.message != null
    }
}
