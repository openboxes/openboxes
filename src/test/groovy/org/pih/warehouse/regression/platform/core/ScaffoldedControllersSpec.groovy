package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.gorm.PagedResultList
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest

import org.pih.warehouse.product.Product

class PartyTypeControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PartyTypeController>, DataTest {
    Class getDomainClass() { PartyType }
    List<Class> getMockedDomains() { [PartyType] }
    String getInstanceKey() { "partyTypeInstance" }
    List<String> getListKeys() { ["partyTypeInstanceList", "partyTypeInstanceTotal"] }
    Map getValidParams() { [code: "ORG", name: "Organization", partyTypeCode: "ORGANIZATION"] }
    Map getInvalidParams() { [partyTypeCode: "BOGUS"] }
    def createInstance() { new PartyType(code: "PER", name: "Person", partyTypeCode: PartyTypeCode.PERSON).save(failOnError: true) }
    String saveRedirect(def id) { "/partyType/list/${id}" }
    String updateRedirect(def id) { "/partyType/list/${id}" }

    void "update refuses a stale version"() {
        given:
        PartyType instance = createInstance()
        instance.version = 4

        request.method = "POST"

        when:
        params.id = instance.id
        params.version = "1"
        params.name = "Stale"
        controller.update()

        then:
        view == "/partyType/edit"
        model.partyTypeInstance.errors.getFieldError("version").code == "default.optimistic.locking.failure"
    }
}

class PartyRoleControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PartyRoleController>, DataTest {
    Party party
    Class getDomainClass() { PartyRole }
    List<Class> getMockedDomains() { [PartyRole, Party, PartyType] }
    String getInstanceKey() { "partyRoleInstance" }
    List<String> getListKeys() { ["partyRoleInstanceList", "partyRoleInstanceTotal"] }
    Party getParty() {
        if (!party) {
            party = new Party(partyType: new PartyType(code: "ORG", name: "Org", partyTypeCode: PartyTypeCode.ORGANIZATION).save(failOnError: true)).save(failOnError: true)
        }
        return party
    }
    Map getValidParams() { ["party.id": getParty().id, roleType: "ROLE_SUPPLIER"] }
    Map getInvalidParams() { [roleType: "NOT_A_ROLE"] }
    def createInstance() { new PartyRole(party: getParty(), roleType: RoleType.ROLE_MANUFACTURER).save(failOnError: true) }
    String saveRedirect(def id) { "/partyRole/list/${id}" }
    String updateRedirect(def id) { "/partyRole/list/${id}" }
}

class PartyControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PartyController>, DataTest {
    PartyType partyType
    Class getDomainClass() { Party }
    List<Class> getMockedDomains() { [Party, PartyType] }
    String getInstanceKey() { "partyInstance" }
    List<String> getListKeys() { ["partyInstanceList", "partyInstanceTotal"] }
    PartyType getPartyType() {
        if (!partyType) {
            partyType = new PartyType(code: "ORG", name: "Org", partyTypeCode: PartyTypeCode.ORGANIZATION).save(failOnError: true)
        }
        return partyType
    }
    Map getValidParams() { ["partyType.id": getPartyType().id] }
    Map getInvalidParams() { ["partyType.id": ""] }
    def createInstance() { new Party(partyType: getPartyType()).save(failOnError: true) }
    String saveRedirect(def id) { "/party/list/${id}" }
    String updateRedirect(def id) { "/party/list/${id}" }
}

class EventTypeControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<EventTypeController>, DataTest {
    Class getDomainClass() { EventType }
    List<Class> getMockedDomains() { [EventType] }
    String getInstanceKey() { "eventTypeInstance" }
    List<String> getListKeys() { ["eventTypeInstanceList", "eventTypeInstanceTotal"] }
    Map getValidParams() { [name: "Picked", eventCode: "PICKED"] }
    Map getInvalidParams() { [eventCode: "BOGUS"] }
    def createInstance() { new EventType(name: "Scheduled", eventCode: EventCode.SCHEDULED).save(failOnError: true) }
    String saveRedirect(def id) { "/eventType/list/${id}" }
    String updateRedirect(def id) { "/eventType/list/${id}" }

}

class GlAccountTypeControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<GlAccountTypeController>, DataTest {
    Class getDomainClass() { GlAccountType }
    List<Class> getMockedDomains() { [GlAccountType] }
    String getInstanceKey() { "glAccountType" }
    List<String> getListKeys() { ["glAccountTypes", "glAccountTypesTotal"] }
    Map getValidParams() { [code: "EXP", name: "Expenses", glAccountTypeCode: "EXPENSE"] }
    Map getInvalidParams() { [code: ""] }
    def createInstance() { new GlAccountType(code: "AST", name: "Assets", glAccountTypeCode: GlAccountTypeCode.ASSET).save(failOnError: true) }
    String saveRedirect(def id) { "/glAccountType/edit/${id}" }
    String updateRedirect(def id) { "/glAccountType/list" }
    boolean getMissingRedirects() { false }
    boolean getHasShow() { false }
}

class GlAccountControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<GlAccountController>, DataTest {
    GlAccountType type
    Class getDomainClass() { GlAccount }
    List<Class> getMockedDomains() { [GlAccount, GlAccountType, Product] }
    String getInstanceKey() { "glAccount" }
    List<String> getListKeys() { ["glAccounts", "glAccountsTotal"] }
    GlAccountType getType() {
        if (!type) {
            type = new GlAccountType(code: "EXP", name: "Expenses", glAccountTypeCode: GlAccountTypeCode.EXPENSE).save(failOnError: true)
        }
        return type
    }
    Map getValidParams() { [code: "6000", name: "Medical supplies", "glAccountType.id": getType().id, active: "true"] }
    Map getInvalidParams() { [code: "", name: "No code"] }
    def createInstance() { new GlAccount(code: "5000", name: "Freight", glAccountType: getType()).save(failOnError: true) }
    String saveRedirect(def id) { "/glAccount/edit/${id}" }
    String updateRedirect(def id) { "/glAccount/list" }
    boolean getMissingRedirects() { false }
    boolean getHasShow() { false }

    void "edit exposes the account type id"() {
        given:
        GlAccount account = createInstance()

        when:
        params.id = account.id
        def model = controller.edit()

        then:
        model.glAccountTypeId == getType().id
    }

    void "an account used by a product cannot be deactivated"() {
        given:
        GlAccount account = createInstance()
        new Product(name: "Gauze", glAccount: account).save(validate: false)
        request.method = "POST"

        when:
        params.id = account.id
        params.name = "Renamed"
        controller.update()

        then:
        flash.message == "This GL account is linked to an active product and cannot be deactivated."
        response.redirectedUrl == "/glAccount/list"
        account.name == "Freight"
    }

    void "update reports an unknown account"() {
        when:
        params.id = "missing"
        controller.update()

        then:
        flash.message.startsWith("default.not.found.message")
        response.redirectedUrl == "/glAccount/list"
    }
}

class PreferenceTypeControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PreferenceTypeController>, DataTest {
    Class getDomainClass() { PreferenceType }
    List<Class> getMockedDomains() { [PreferenceType] }
    String getInstanceKey() { "preferenceType" }
    List<String> getListKeys() { ["preferenceTypes", "preferenceTypesTotal"] }
    Map getValidParams() { [name: "Preferred", validationCode: "WARN"] }
    Map getInvalidParams() { [name: ""] }
    def createInstance() { new PreferenceType(name: "Blocked", validationCode: ValidationCode.BLOCK).save(failOnError: true) }
    String saveRedirect(def id) { "/preferenceType/edit/${id}" }
    String updateRedirect(def id) { "/preferenceType/list" }
    boolean getMissingRedirects() { false }
    boolean getHasShow() { false }
}

class PaymentTermControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PaymentTermController>, DataTest {
    Class getDomainClass() { PaymentTerm }
    List<Class> getMockedDomains() { [PaymentTerm] }
    String getInstanceKey() { "paymentTerm" }
    List<String> getListKeys() { ["paymentTerms", "paymentTermsTotal"] }
    Map getValidParams() { [code: "NET30", name: "Net 30", daysToPayment: "30"] }
    Map getInvalidParams() { [code: "", name: ""] }
    def createInstance() { new PaymentTerm(code: "NET60", name: "Net 60").save(failOnError: true) }
    String saveRedirect(def id) { "/paymentTerm/edit/${id}" }
    String updateRedirect(def id) { "/paymentTerm/list" }
    boolean getHasIndex() { false }
    String getUpdateAction() { "save" }
    boolean getMissingRedirects() { false }
    boolean getHasShow() { false }

}

class BudgetCodeControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<BudgetCodeController>, DataTest {
    Class getDomainClass() { BudgetCode }
    List<Class> getMockedDomains() { [BudgetCode, Organization] }
    String getInstanceKey() { "budgetCode" }
    List<String> getListKeys() { null }
    Map getValidParams() { [code: "BC-1", name: "Operations"] }
    Map getInvalidParams() { [code: ""] }
    def createInstance() { new BudgetCode(code: "BC-0", name: "Capital").save(failOnError: true) }
    String saveRedirect(def id) { "/budgetCode/edit/${id}" }
    String updateRedirect(def id) { "/budgetCode/list" }
    boolean getMissingRedirects() { false }
    boolean getHasShow() { false }

    void "list renders the budget codes found by the filter"() {
        given:
        BudgetCode code = createInstance()
        controller.budgetCodeService = Mock(BudgetCodeServiceStub)

        when:
        def model = controller.list(new BudgetCodeFilterCommand())

        then:
        1 * controller.budgetCodeService.getBudgetCodes(_) >> new PagedResultList(null).with {
            resultList = [code]
            totalCount = 1
            it
        }
        model.budgetCodes == [code]
        model.budgetCodesTotal == 1
    }

    void "edit exposes the organization id"() {
        given:
        Organization organization = new Organization(code: "ORG", name: "Org").save(validate: false)
        BudgetCode code = new BudgetCode(code: "BC-9", organization: organization).save(failOnError: true)

        when:
        params.id = code.id
        def model = controller.edit()

        then:
        model.organizationId == organization.id
    }
}

interface BudgetCodeServiceStub {
    def getBudgetCodes(def command)
}
