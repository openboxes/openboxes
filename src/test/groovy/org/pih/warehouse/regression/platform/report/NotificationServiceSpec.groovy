package org.pih.warehouse.regression.platform.report

import spock.lang.PendingFeature

import spock.lang.Ignore
import org.pih.warehouse.report.*
import grails.core.GrailsApplication
import grails.testing.gorm.DataTest
import org.apache.commons.mail.EmailException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.data.DataService
import org.pih.warehouse.data.FileGenerationService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.receiving.GoodsReceiptNoteDto
import org.pih.warehouse.receiving.GoodsReceiptNoteService
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * The service is a spy so that template rendering (which needs a web request and the GSP engine) is replaced by a
 * stub that echoes the template name; everything else runs for real against mocked collaborators.
 */
class NotificationServiceSpec extends Specification implements DataTest {

    NotificationService service
    DataService dataService = Mock()
    UserService userService = Mock()
    MailService mailService = Mock()
    FileGenerationService fileGenerationService = Mock()
    GoodsReceiptNoteService goodsReceiptNoteService = Mock()
    MessageLocalizer messageLocalizer = Mock()
    MessageSource messageSource = Mock()

    Location depot = new Location(name: 'Main depot')

    void setupSpec() {
        mockDomains(Person)
    }

    void setup() {
        depot.id = 'depot-1'
        service = Spy(NotificationService)
        service.renderTemplate(_ as String, _ as Map) >> { String template, Map model -> "rendered ${template}".toString() }
        service.dataService = dataService
        service.userService = userService
        service.mailService = mailService
        service.fileGenerationService = fileGenerationService
        service.goodsReceiptNoteService = goodsReceiptNoteService
        service.messageLocalizer = messageLocalizer
        service.messageSource = messageSource
        service.grailsApplication = grailsApplication
        originalDefaultLocale = config.openboxes.locale.defaultLocale
        config.openboxes.locale.defaultLocale = 'fr'
    }

    Object originalDefaultLocale

    void cleanup() {
        config.openboxes.locale.defaultLocale = originalDefaultLocale
    }

    private static User user(String email) {
        return new User(username: email ?: 'nomail', email: email)
    }

    void 'expiry alerts query the expiry view for the location and the day threshold'() {
        when:
        List result = service.getExpiryAlertsByLocation(depot, 30)

        then:
        1 * dataService.executeQuery({ String sql ->
            sql.contains('from product_inventory_expiry_view') &&
                    sql.contains('days_until_expiry <= 30') &&
                    sql.contains("location_id = 'depot-1'") &&
                    sql.contains('quantity_on_hand > 0')
        }) >> [[product_code: 'P1']]
        result == [[product_code: 'P1']]
    }

    void 'stock alerts query the compare view for the location'() {
        when:
        List result = service.getStockAlertsByLocation(depot)

        then:
        1 * dataService.executeQuery({ String sql ->
            sql.contains('from product_inventory_compare_view') && sql.contains("location_id = 'depot-1'")
        }) >> []
        result == []
    }

    void 'expiry alerts are skipped without looking up subscribers when there are none and skipOnEmpty is set'() {
        when:
        service.sendExpiryAlerts(depot, 60, [RoleType.ROLE_ITEM_EXPIRY_NOTIFICATION], true)

        then:
        1 * dataService.executeQuery(_ as String) >> []
        0 * userService._
        0 * mailService._
    }

    @PendingFeature(reason = 'OBAF attaches the alert csv with the platform charset and a bare text/csv type')
    void 'expiry alerts split expired from expiring items and mail the csv to the subscribers'() {
        given:
        List alerts = [[product: 'A', days_until_expiry: -3], [product: 'B', days_until_expiry: 0],
                       [product: 'C', days_until_expiry: 12]]
        List<RoleType> roleTypes = [RoleType.ROLE_ITEM_EXPIRY_NOTIFICATION]

        when:
        service.sendExpiryAlerts(depot, 60, roleTypes, true)

        then:
        1 * dataService.executeQuery(_ as String) >> alerts
        1 * userService.findUsersByRoleTypes(depot, roleTypes) >> [user('a@example.org'), user(null)]
        1 * dataService.generateCsv(alerts) >> 'product\nCaf\u00e9\n'
        1 * service.renderTemplate('/email/expiryAlerts', { Map model ->
            model.location == depot &&
                    model.daysUntilExpiry == 60 &&
                    model.expired*.product == ['A', 'B'] &&
                    model.expiring*.product == ['C']
        }) >> 'body'
        1 * mailService.sendHtmlMailWithAttachment(['a@example.org'], [], 'Expiry Alerts - Main depot', 'body',
                { Arrays.equals(it as byte[], 'product\nCaf\u00e9\n'.getBytes('UTF-8')) }, 'Expiry Alerts - Main depot.csv',
                'text/csv; charset=UTF-8')
    }

    void 'expiry alerts are still mailed without alerts when skipOnEmpty is not set'() {
        when:
        service.sendExpiryAlerts(depot, 60, [RoleType.ROLE_ITEM_EXPIRY_NOTIFICATION], false)

        then:
        1 * dataService.executeQuery(_ as String) >> []
        1 * userService.findUsersByRoleTypes(depot, _) >> [user('a@example.org')]
        1 * dataService.generateCsv([]) >> ''
        // An empty csv means no attachment
        1 * mailService.sendHtmlMail('Expiry Alerts - Main depot', 'rendered /email/expiryAlerts', ['a@example.org'])
        0 * mailService.sendHtmlMailWithAttachment(*_)
    }

    @PendingFeature(reason = 'OBAF attaches the alert csv as bare text/csv, without charset=UTF-8')
    void 'stock alerts only include products with the requested status'() {
        given:
        List rows = [[product: 'A', status: 'low_stock'], [product: 'B', status: 'over_stock'],
                     [product: 'C', status: 'low_stock']]

        when:
        service.sendStockAlerts(depot, 'low_stock', [RoleType.ROLE_ITEM_LOW_STOCK_NOTIFICATION], true)

        then:
        1 * dataService.executeQuery(_ as String) >> rows
        1 * userService.findUsersByRoleTypes(depot, [RoleType.ROLE_ITEM_LOW_STOCK_NOTIFICATION]) >> [user('a@example.org')]
        1 * dataService.generateCsv({ it*.product == ['A', 'C'] }) >> 'csv'
        1 * service.renderTemplate('/email/stockAlerts', { it.status == 'low_stock' && it.products*.product == ['A', 'C'] }) >> 'body'
        1 * mailService.sendHtmlMailWithAttachment(['a@example.org'], [], 'Stock Alerts - Status low_stock - Main depot',
                'body', { new String(it as byte[], 'UTF-8') == 'csv' }, 'Stock Alerts - Status low_stock - Main depot.csv',
                'text/csv; charset=UTF-8')
    }

    void 'stock alerts are skipped when no product has the status and skipOnEmpty is set'() {
        when:
        service.sendStockAlerts(depot, 'out_of_stock', [], true)

        then:
        1 * dataService.executeQuery(_ as String) >> [[product: 'A', status: 'low_stock']]
        0 * userService._
        0 * mailService._
    }

    void 'alerts are not rendered or sent when no subscriber has an email address'() {
        when:
        service.sendAlerts('Subject', '/email/x', [:], [user(null)], 'csv')

        then:
        0 * service.renderTemplate(*_)
        0 * mailService._
    }

    @Unroll
    void 'shipment #event notification uses subject "#subject" and template #template'() {
        given:
        Shipment shipment = new Shipment(shipmentNumber: 'S-100')
        List<RoleType> roleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION]

        when:
        service."$method"(shipment, depot, roleTypes)

        then:
        1 * userService.findUsersByRoleTypes(depot, roleTypes) >> [user('a@example.org'), user('b@example.org')]
        1 * service.renderTemplate(template, [shipmentInstance: shipment]) >> 'body'
        1 * mailService.sendHtmlMail(subject, 'body', ['a@example.org', 'b@example.org'])

        where:
        event      | method                            || subject                            | template
        'created'  | 'sendShipmentCreatedNotification' || 'Shipment S-100 has been created'  | '/email/shipmentCreated'
        'issued'   | 'sendShipmentIssuedNotification'  || 'Shipment S-100 has been shipped'  | '/email/shipmentShipped'
        'received' | 'sendShipmentReceiptNotification' || 'Shipment S-100 has been received' | '/email/shipmentReceived'
    }

    void 'shipment notifications are not sent when there are no subscribers'() {
        when:
        service.sendShipmentCreatedNotification(new Shipment(shipmentNumber: 'S-100'), depot, [])

        then:
        1 * userService.findUsersByRoleTypes(depot, []) >> []
        0 * mailService._
    }

    void 'items shipped notification sends one mail per recipient with a valid email address'() {
        given:
        Person alice = new Person(firstName: 'Alice', lastName: 'A', email: 'alice@example.org')
        Person bob = new Person(firstName: 'Bob', lastName: 'B', email: 'not-an-email')
        ShipmentItem aliceItem1 = new ShipmentItem(recipient: alice, quantity: 1)
        ShipmentItem aliceItem2 = new ShipmentItem(recipient: alice, quantity: 2)
        ShipmentItem bobItem = new ShipmentItem(recipient: bob, quantity: 3)
        ShipmentItem nobodyItem = new ShipmentItem(quantity: 4)
        Shipment shipment = Stub(Shipment) {
            getShipmentNumber() >> 'S-7'
            getOrigin() >> new Location(name: 'Origin')
            getDestination() >> new Location(name: 'Destination')
            getShipmentItems() >> ([aliceItem1, aliceItem2, bobItem, nobodyItem] as LinkedHashSet)
        }

        and: 'a tag library that records what it was asked to render'
        List messageCalls = []
        List renderCalls = []
        Expando g = new Expando(
                message: { Map attrs -> messageCalls << attrs; 'Your items shipped' },
                render: { Map attrs -> renderCalls << attrs; 'html' })
        service.grailsApplication = Stub(GrailsApplication) {
            getMainContext() >> Stub(ApplicationContext) {
                getBean('org.grails.plugins.web.taglib.ApplicationTagLib') >> g
            }
        }

        when:
        service.sendShipmentItemsShippedNotification(shipment)

        then:
        1 * mailService.sendHtmlMail('Your items shipped', 'html', 'alice@example.org')
        0 * mailService._
        messageCalls == [[code: 'email.yourItemShipped.message', args: ['Origin', 'Destination', 'S-7']]]
        renderCalls.size() == 1
        renderCalls[0].template == '/email/shipmentItemShipped'
        renderCalls[0].model.recipient == alice
        renderCalls[0].model.shipmentItems == [aliceItem1, aliceItem2]
    }

    void 'partial receipt notification mails each valid recipient a goods receipt note and removes the barcode file'() {
        given:
        Person receiver = new Person(firstName: 'Rita', lastName: 'R', email: 'rita@example.org')
        Person alice = new Person(firstName: 'Alice', lastName: 'A', email: 'alice@example.org')
        Shipment shipment = new Shipment(shipmentNumber: 'S-9', destination: new Location(name: 'Clinic'))
        shipment.id = 'shipment-9'
        GoodsReceiptNoteDto goodsReceiptNote = new GoodsReceiptNoteDto(shipmentNumber: 'S-9')
        PartialReceiptItem aliceItem = new PartialReceiptItem(recipient: alice)
        PartialReceiptItem anonymousItem = new PartialReceiptItem()
        PartialReceipt partialReceipt = new PartialReceipt(shipment: shipment, recipient: receiver,
                partialReceiptContainers: [new PartialReceiptContainer(partialReceiptItems: [aliceItem, anonymousItem])])

        and: 'a barcode file on disk'
        File barcodeFile = File.createTempFile('barcode-test', '.png')
        byte[] pdf = [1, 2, 3] as byte[]
        List localizedArgs = null
        ApplicationTagLib tagLib = GroovyMock(ApplicationTagLib)
        service.getApplicationTagLib() >> tagLib

        when:
        service.sendReceiptNotifications(partialReceipt)

        then:
        1 * goodsReceiptNoteService.buildGoodsReceiptNote('shipment-9') >> goodsReceiptNote
        1 * messageLocalizer.localize('email.yourItemReceived.message', *_) >> { arguments ->
            localizedArgs = arguments.drop(1).flatten()
            'Items received'
        }
        1 * tagLib.render([template: '/email/shipmentItemReceived',
                           model   : [shipmentInstance: shipment, receiptItems: [aliceItem], recipient: alice, receivedBy: receiver]]) >> 'html'
        1 * fileGenerationService.generateBarcodeFile('S-9') >> barcodeFile
        1 * fileGenerationService.getFileUri(barcodeFile) >> 'file:///barcode.png'
        1 * fileGenerationService.generatePdfFromTemplate('/email/goodsDeliveryNote',
                [goodsReceiptNote: goodsReceiptNote, barcodeFileUri: 'file:///barcode.png']) >> pdf
        1 * mailService.sendHtmlMailWithAttachment('alice@example.org', 'Items received', 'html', pdf,
                'GoodsReceiptNote-S-9.pdf', 'application/pdf')
        0 * mailService._
        localizedArgs == ['Clinic', 'S-9']
        !barcodeFile.exists()

        cleanup:
        barcodeFile?.delete()
    }

    @Unroll
    void 'application errors are not mailed for #description locations'() {
        given:
        Location location = new Location(name: 'Depot', active: active, supportedActivities: activities as Set)

        when:
        service.sendApplicationErrorNotification(location, new RuntimeException('boom'))

        then:
        0 * userService._
        0 * mailService._

        where:
        description                  | active | activities
        'inactive'                   | false  | [ActivityCode.ENABLE_NOTIFICATIONS.id]
        'notification-disabled'      | true   | [ActivityCode.MANAGE_INVENTORY.id]
    }

    void 'application errors are mailed to the error notification subscribers of an active location'() {
        given:
        Location location = new Location(name: 'Depot', active: true,
                supportedActivities: [ActivityCode.ENABLE_NOTIFICATIONS.id] as Set)
        RuntimeException exception = new RuntimeException('boom')

        when:
        service.sendApplicationErrorNotification(location, exception)

        then:
        1 * userService.findUsersByRoleTypes(location, [RoleType.ROLE_ERROR_NOTIFICATION]) >> [user('ops@example.org')]
        1 * service.renderTemplate('/email/applicationError', { it.location == location && it.exception.cause == exception }) >> 'body'
        1 * mailService.sendHtmlMail('Application Error: boom', 'body', ['ops@example.org'])
    }

    void 'user account creation is mailed once to every distinct notification recipient in the default locale'() {
        given:
        User newUser = new User(username: 'jdoe')
        Map questions = [reason: 'testing']

        when:
        service.sendUserAccountCreation(newUser, questions)

        then:
        1 * userService.findUsersByRoleType(RoleType.ROLE_USER_NOTIFICATION) >>
                [user('admin@example.org'), user('admin@example.org'), user('boss@example.org')]
        1 * messageSource.getMessage('email.userAccountCreated.message', ['jdoe'] as Object[], new Locale('fr')) >> 'New account'
        1 * service.renderTemplate('/email/userAccountCreated', [userInstance: newUser, additionalQuestions: questions]) >> 'body'
        1 * mailService.sendHtmlMail('New account', 'body', ['admin@example.org', 'boss@example.org'])
    }

    void 'user account creation is not mailed when nobody subscribes to user notifications'() {
        when:
        service.sendUserAccountCreation(new User(username: 'jdoe'), [:])

        then:
        1 * userService.findUsersByRoleType(RoleType.ROLE_USER_NOTIFICATION) >> []
        0 * mailService._
    }

    void 'a mail failure during user account creation is logged instead of thrown'() {
        given:
        userService.findUsersByRoleType(_) >> [user('admin@example.org')]
        mailService.sendHtmlMail(*_) >> { throw new EmailException('smtp down') }

        when:
        service.sendUserAccountCreation(new User(username: 'jdoe'), [:])

        then:
        noExceptionThrown()
    }

    void 'user account confirmation is mailed to the user in #description'() {
        given:
        User newUser = new User(username: 'jdoe', email: 'jdoe@example.org', locale: userLocale)

        when:
        service.sendUserAccountConfirmation(newUser, [:])

        then:
        1 * messageSource.getMessage('email.userAccountConfirmed.message', ['jdoe@example.org'] as Object[], expectedLocale) >> 'Confirmed'
        1 * mailService.sendHtmlMail('Confirmed', 'rendered /email/userAccountConfirmed', 'jdoe@example.org')

        where:
        description              | userLocale        || expectedLocale
        'their own locale'       | new Locale('es')  || new Locale('es')
        'the default locale'     | null              || new Locale('fr')
    }

    void 'user account confirmation is skipped for a user without an email address and survives mail failures'() {
        when:
        service.sendUserAccountConfirmation(new User(username: 'jdoe'), [:])

        then:
        0 * mailService._

        when:
        service.sendUserAccountConfirmation(new User(username: 'jdoe', email: 'jdoe@example.org'), [:])

        then:
        1 * messageSource.getMessage(*_) >> 'Confirmed'
        1 * mailService.sendHtmlMail(*_) >> { throw new EmailException('smtp down') }
        noExceptionThrown()
    }

    void 'pending approval notifies every approver with an email address, linking to their approvals list'() {
        given:
        Person approver = new Person(firstName: 'Ann', lastName: 'Approver', email: 'ann@example.org').save(validate: false)
        Person silent = new Person(firstName: 'Sam', lastName: 'Silent').save(validate: false)
        Requisition requisition = new Requisition(requestNumber: 'R-1', name: 'Monthly',
                status: RequisitionStatus.PENDING_APPROVAL, approvers: [approver, silent] as Set)

        when:
        service.publishRequisitionStatusTransitionNotifications(requisition)

        then:
        1 * service.renderTemplate('/email/approvalsAlert', [requisition: requisition,
                redirectUrl: "/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC&approver=${approver.id}".toString()]) >> 'body'
        1 * mailService.sendHtmlMail('R-1 Monthly', 'body', 'ann@example.org')
        0 * mailService._
    }

    void 'pending approval without approvers sends nothing'() {
        when:
        service.publishRequisitionStatusTransitionNotifications(
                new Requisition(requestNumber: 'R-1', name: 'Monthly', status: RequisitionStatus.PENDING_APPROVAL))

        then:
        0 * mailService._
    }

    @Unroll
    void 'a #status requisition notifies the requester of the status change'() {
        given:
        Requisition requisition = new Requisition(requestNumber: 'R-2', name: 'Weekly', status: status,
                requestedBy: new Person(firstName: 'Rob', lastName: 'Requester', email: 'rob@example.org'))

        when:
        service.publishRequisitionStatusTransitionNotifications(requisition)

        then:
        1 * service.renderTemplate('/email/approvalsStatusChanged', [requisition: requisition]) >> 'body'
        1 * mailService.sendHtmlMail('R-2 Weekly', 'body', 'rob@example.org')

        where:
        status << [RequisitionStatus.APPROVED, RequisitionStatus.REJECTED]
    }

    void 'a status change is not mailed to a requester without an email address'() {
        when:
        service.publishRequisitionStatusTransitionNotifications(new Requisition(requestNumber: 'R-2', name: 'Weekly',
                status: RequisitionStatus.APPROVED, requestedBy: new Person(firstName: 'Rob', lastName: 'Requester')))

        then:
        0 * service.renderTemplate(*_)
        0 * mailService._
    }

    @Unroll
    void 'an issued #sourceType requisition sends #mails fulfillment mail(s)'() {
        given:
        Requisition requisition = new Requisition(requestNumber: 'R-3', name: 'Urgent', status: RequisitionStatus.ISSUED,
                sourceType: sourceType, requestedBy: new Person(firstName: 'Rob', lastName: 'Requester', email: 'rob@example.org'))

        when:
        service.publishRequisitionStatusTransitionNotifications(requisition)

        then:
        mails * mailService.sendHtmlMail('R-3 Urgent', 'rendered /email/fulfillmentAlert', 'rob@example.org')

        where:
        sourceType                        || mails
        RequisitionSourceType.ELECTRONIC  || 1
        RequisitionSourceType.PAPER       || 0
        null                              || 0
    }

    void 'requisition statuses without a notification send nothing'() {
        when:
        service.publishRequisitionStatusTransitionNotifications(new Requisition(status: RequisitionStatus.PICKING,
                requestedBy: new Person(email: 'rob@example.org')))

        then:
        0 * mailService._
    }

    void 'a fulfillment notification is not sent to a requester without an email address'() {
        when:
        service.publishFulfillmentNotification(new Person(firstName: 'Rob', lastName: 'R'), new Requisition(requestNumber: 'R-3'))

        then:
        0 * mailService._
    }
}
