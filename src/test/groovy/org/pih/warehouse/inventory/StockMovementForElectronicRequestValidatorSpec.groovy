package org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType

@Unroll
class StockMovementForElectronicRequestValidatorSpec extends Specification implements DataTest {

    StockMovementForElectronicRequestValidator validator = new StockMovementForElectronicRequestValidator()

    Location ward

    Location depot

    void setupSpec() {
        mockDomains(Location)
    }

    void setup() {
        ward = new Location(name: 'Ward').save(validate: false)
        depot = new Location(name: 'Depot').save(validate: false)
    }

    void cleanup() {
        // The current location is a static thread-local, so it would leak into the next spec
        new AuthService().setCurrentLocation(null)
    }

    void 'doValidate should reject #scenario e-request saved from another location'() {
        given:
        new AuthService().setCurrentLocation(depot)

        when:
        ObjectValidationResult result = validator.doValidate(buildStockMovement(sourceType, requisitionSourceType))

        then:
        assert result.errors*.code == ["stockMovement.request.isDifferentLocation.message"]

        where:
        scenario      | sourceType                       | requisitionSourceType
        'a new'       | RequisitionSourceType.ELECTRONIC | null
        'an existing' | null                             | RequisitionSourceType.ELECTRONIC
    }

    void 'doValidate should reject an existing e-request with another destination in the request body'() {
        given:
        new AuthService().setCurrentLocation(depot)

        when:
        ObjectValidationResult result = validator.doValidate(
                buildStockMovement(null, RequisitionSourceType.ELECTRONIC, depot))

        then:
        assert result.errors*.code == ["stockMovement.request.isDifferentLocation.message"]
    }

    void 'doValidate should pass for #scenario'() {
        given:
        new AuthService().setCurrentLocation(isLoggedInAtWard ? ward : depot)

        expect:
        validator.doValidate(buildStockMovement(null, sourceType)).valid

        where:
        scenario                                          | sourceType                       | isLoggedInAtWard
        'an e-request saved from its requesting location' | RequisitionSourceType.ELECTRONIC | true
        'a paper request saved from the origin'           | RequisitionSourceType.PAPER      | false
    }

    void 'validateForStatusTransition should reject submitting an e-request as #status from another location'() {
        given:
        new AuthService().setCurrentLocation(depot)

        when:
        validator.validateForStatusTransition(buildStockMovement(null, RequisitionSourceType.ELECTRONIC), status)

        then:
        thrown(ValidationException)

        where:
        status << [StockMovementStatusCode.REQUESTED, StockMovementStatusCode.PENDING_APPROVAL]*.name()
    }

    void 'validateForStatusTransition should let the origin approve an e-request'() {
        given:
        new AuthService().setCurrentLocation(depot)

        when:
        validator.validateForStatusTransition(buildStockMovement(null, RequisitionSourceType.ELECTRONIC),
                StockMovementStatusCode.APPROVED.name())

        then:
        notThrown(ValidationException)
    }

    /**
     * The destination is the one bound from the request body, while the requisition keeps the saved one (the ward).
     */
    private StockMovement buildStockMovement(RequisitionSourceType sourceType,
                                             RequisitionSourceType requisitionSourceType,
                                             Location destination = ward) {
        return new StockMovement(
                destination: destination,
                sourceType: sourceType,
                requisition: requisitionSourceType ?
                        new Requisition(sourceType: requisitionSourceType, destination: ward) : null,
        )
    }
}
