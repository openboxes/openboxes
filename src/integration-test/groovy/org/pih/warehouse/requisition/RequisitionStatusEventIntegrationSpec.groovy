package org.pih.warehouse.requisition

import java.lang.reflect.Field

import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.persister.entity.EntityPersister
import org.hibernate.property.access.spi.Setter
import org.hibernate.property.access.spi.SetterFieldImpl
import org.hibernate.tuple.entity.AbstractEntityTuplizer

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

/**
 * Verifies that overriding the persistent property setter Requisition.setStatus (OBLS-929) does not leak
 * its Event-creating side effect into Hibernate entity hydration. Hibernate's default access strategy for
 * a mapped property is "property" access, meaning it calls the setter when populating an entity read from
 * the database - which would run recordStatusChange() with a null oldStatus on every Requisition.get().
 */
@Rollback
class RequisitionStatusEventIntegrationSpec extends IntegrationSpec {

    SessionFactory sessionFactory

    void 'hibernate does not invoke the overridden setStatus when hydrating a requisition'() {
        given: 'a persisted requisition whose status transition event has already been recorded'
        Location origin = new LocationTestBuilder().findOrBuildMainFacility()
        Location destination = new LocationTestBuilder().name("Test Destination ${UUID.randomUUID()}").build(true)
        Requisition requisition = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: Person.build(),
                status: RequisitionStatus.CREATED,
        )
        Requisition.withSession { it.flush() }
        String requisitionId = requisition.id
        int eventCountAfterInsert = countEvents(requisitionId)

        and: 'the session is cleared so the read below is a real hydration and not a first-level cache hit'
        Requisition.withSession { it.clear() }

        when: 'the requisition is read back from the database'
        Requisition reloaded = Requisition.get(requisitionId)
        reloaded.status
        Requisition.withSession { it.flush() }

        then: 'hydration did not record a second transition event'
        reloaded.status == RequisitionStatus.CREATED
        countEvents(requisitionId) == eventCountAfterInsert
    }

    void 'the status property is mapped with field access so the overridden setter is bypassed on load'() {
        given:
        EntityPersister persister = ((SessionFactoryImplementor) sessionFactory)
                .metamodel
                .entityPersister(Requisition.name)
        int statusIndex = persister.entityMetamodel.getPropertyIndex('status')

        when:
        AbstractEntityTuplizer tuplizer = (AbstractEntityTuplizer) persister.entityTuplizer
        Field settersField = AbstractEntityTuplizer.getDeclaredField('setters')
        settersField.accessible = true
        Setter statusSetter = ((Setter[]) settersField.get(tuplizer))[statusIndex]

        then: 'a SetterMethodImpl here means Hibernate calls Requisition.setStatus() during hydration'
        statusSetter instanceof SetterFieldImpl
    }

    private int countEvents(String requisitionId) {
        Requisition.executeQuery(
                'select count(e) from Requisition r join r.events e where r.id = :id',
                [id: requisitionId],
        )[0] as int
    }
}
