package org.pih.warehouse.order

import grails.databinding.BindUsing
import grails.util.Holders
import grails.validation.Validateable
import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Party
import org.pih.warehouse.core.PaymentMethodType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User

import java.time.Instant

class CreatePurchaseOrderCommand implements Validateable {

    String orderNumber

    String name

    @BindUsing({ obj, source ->
        def id = source['origin']['id']
        def name = source['origin']['name']
        return Location.findByIdOrName(id, name)
    })
    Location origin

    @BindUsing({ obj, source ->
        def id = source['destination']['id']
        def name = source['destination']['name']
        return Location.findByIdOrName(id, name)
    })
    Location destination

    @BindUsing({ obj, source ->
        def id = source['destinationParty']['id']
        def name = source['destinationParty']['name']
        return Party.findByIdOrName(id, name)
    })
    Party destinationParty

    @BindUsing({ obj, source ->
        def date = source['dateOrdered']
        return DateUtil.asDate(Instant.parse(date.toString()))
    })
    Date dateOrdered

    @BindUsing({ obj, source ->
        def id = source['orderedBy']['id']
        def username = source['orderedBy']['username']
        return User.findByIdOrUsername(id, username)
    })
    Person orderedBy

    String currencyCode

    @BindUsing({ obj, source ->
        def id = source['paymentMethodType']['id']
        def name = source['paymentMethodType']['name']
        return PaymentMethodType.findByIdOrName(id, name)
    })
    PaymentMethodType paymentMethodType

    @BindUsing({ obj, source ->
        def id = source['paymentTerm']['id']
        def name = source['paymentTerm']['name']
        return PaymentTerm.findByIdOrName(id, name)
    })
    PaymentTerm paymentTerm

    List<CreatePurchaseOrderItemCommand> items

    void applyDefaults() {
        if (!name) {
            def parts = []
            if (orderNumber) parts << orderNumber
            if (origin?.name) parts << origin.name
            if (destination?.name) parts << destination.name
            name = parts.join("-")
        }

        if (!dateOrdered) {
            dateOrdered = new Date()
        }

        if (!currencyCode) {
            currencyCode = Holders.grailsApplication.config.openboxes.locale.defaultCurrencyCode
        }
    }

    static constraints = {
        name(nullable: true)
        orderNumber(nullable: true)
        origin(nullable: false)
        destination(nullable: false)
        destinationParty(nullable: false)
        dateOrdered(nullable: true)
        orderedBy(nullable: false)
        currencyCode(nullable: true)
        paymentMethodType(nullable: true)
        paymentTerm(nullable: true)
    }
}
