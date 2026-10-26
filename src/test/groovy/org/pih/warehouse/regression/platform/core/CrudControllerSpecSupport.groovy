package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.MessageTagLib

/**
 * Shared behavior of the scaffolded create/read/update/delete controllers. Concrete specs implement
 * ControllerUnitTest and DataTest for their controller and describe its model keys and redirects.
 */
abstract class CrudControllerSpecSupport extends Specification {

    /** The domain class the controller manages. */
    abstract Class getDomainClass()

    /** Every domain class to mock. */
    abstract List<Class> getMockedDomains()

    /** Model key of a single instance, e.g. partyTypeInstance. */
    abstract String getInstanceKey()

    /** Model keys of the list action: [list, total], or null when the list action is not generic. */
    abstract List<String> getListKeys()

    /** Request parameters that bind into a valid instance. */
    abstract Map getValidParams()

    /** Request parameters that fail validation. */
    abstract Map getInvalidParams()

    /** Saves and returns a persisted instance. */
    abstract def createInstance()

    /** Where a successful save redirects. */
    abstract String saveRedirect(def id)

    /** Where a successful update redirects. */
    abstract String updateRedirect(def id)

    /** Whether show/edit of an unknown id redirects to the list (otherwise they return an empty model). */
    boolean getMissingRedirects() { true }

    boolean getHasShow() { true }

    void setupSpec() {
        mockDomains(mockedDomains as Class[])
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }
    }

    boolean getHasIndex() { true }

    String getUpdateAction() { "update" }

    void "index redirects to the list"() {
        when:
        if (hasIndex) {
            controller.index()
        }

        then:
        !hasIndex || response.redirectedUrl.startsWith(controllerPath + "/list")
    }

    String getControllerPath() {
        String name = domainClass.simpleName
        return "/" + name[0].toLowerCase() + name.substring(1)
    }

    void "list renders the instances with their total"() {
        given:
        def first = createInstance()

        when:
        def model = listKeys ? controller.list() : null

        then:
        !listKeys || model[listKeys[0]]*.id == [first.id]
        !listKeys || model[listKeys[1]] == 1
    }

    void "create binds the request parameters"() {
        when:
        params.putAll(validParams)
        def model = controller.create()

        then:
        model[instanceKey].id == null
        boundAsGiven(model[instanceKey])
    }

    void "save stores a valid instance"() {
        given:
        int before = domainClass.count()
        request.method = "POST"

        when:
        params.putAll(validParams)
        controller.save()
        def saved = domainClass.list().find { candidate -> !(candidate.id in createdIds) }

        then:
        domainClass.count() == before + 1
        response.redirectedUrl == saveRedirect(saved.id)
        flash.message.startsWith(createdMessage)
    }

    String getCreatedMessage() { "default.created.message" }

    List getCreatedIds() { [] }

    boolean boundAsGiven(def instance) {
        return validParams.every { String k, v -> k.contains(".") || instance."$k"?.toString() == v?.toString() }
    }

    void "save redisplays the form for an invalid instance"() {
        given:
        request.method = "POST"

        when:
        params.putAll(invalidParams)
        controller.save()

        then:
        view.endsWith("/create")
        model[instanceKey].hasErrors()
    }

    void "show and edit render an existing instance"() {
        given:
        def instance = createInstance()

        when:
        params.id = instance.id
        def editModel = controller.edit()
        def showModel = hasShow ? controller.show() : [(instanceKey): instance]

        then:
        editModel[instanceKey] == instance
        showModel[instanceKey] == instance
    }

    void "edit of an unknown id"() {
        when:
        params.id = "missing"
        def model = controller.edit()

        then:
        missingRedirects ? response.redirectedUrl == controllerPath + "/list" : model[instanceKey] == null
    }

    void "update saves valid changes"() {
        given:
        def instance = createInstance()
        request.method = "POST"

        when:
        params.putAll(validParams)
        params.id = instance.id
        controller."$updateAction"()

        then:
        response.redirectedUrl == updateRedirect(instance.id)
        flash.message.startsWith("default.updated.message")
        boundAsGiven(instance)
    }

    void "update redisplays the form for invalid changes"() {
        given:
        def instance = createInstance()
        request.method = "POST"

        when:
        params.putAll(invalidParams)
        params.id = instance.id
        controller."$updateAction"()

        then:
        view.endsWith("/edit")
        model[instanceKey].hasErrors()
    }

    void "delete removes an existing instance"() {
        given:
        def instance = createInstance()
        request.method = "POST"

        when:
        params.id = instance.id
        controller.delete()

        then:
        flash.message.startsWith("default.deleted.message")
        response.redirectedUrl == controllerPath + "/list"
    }

    void "delete reports an unknown id"() {
        given:
        request.method = "POST"

        when:
        params.id = "missing"
        controller.delete()

        then:
        flash.message.startsWith("default.not.found.message")
        response.redirectedUrl == controllerPath + "/list"
    }
}
