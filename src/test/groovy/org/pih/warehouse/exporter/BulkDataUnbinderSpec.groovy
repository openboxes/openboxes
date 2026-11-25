package org.pih.warehouse.exporter

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.serialization.Serializable
import org.pih.warehouse.core.serialization.SerializationMapper

@Unroll
class BulkDataUnbinderSpec extends Specification {

    @Shared
    MapperComponentResolver componentResolverStub

    @Shared
    BulkDataUnbinder unbinder

    void setup() {
        componentResolverStub = Stub(MapperComponentResolver)
        unbinder = new BulkDataUnbinder(componentResolverStub)
    }

    void "unbindData works for an object that has a SerializationMapper"() {
        given:
        List<DummyClass> toUnbind = [new DummyClass(string: "A")]

        and:
        hasSerializationMapper(true)

        expect:
        assert unbinder.unbindData(toUnbind) == [[test: "used SerializationMapper"]]
    }

    void "unbindData works for an object with neither a SerializationMapper nor HttpSerializable"() {
        given:
        List<DummyClass> toUnbind = [new DummyClass(
                parentString : "B",
                string       : "A",
                integer      : 1,
                bool         : true,
                doub         : 2.2,
                date         : new Date(),
                localDate    : LocalDate.now(),
                zonedDateTime: ZonedDateTime.now(),
                instant      : Instant.now(),
        )]

        and:
        hasSerializationMapper(false)

        expect: "Only the declared fields are included, with all of their values unchanged"
        unbinder.unbindData(toUnbind) == [[
                parentString : toUnbind[0].parentString,
                string       : toUnbind[0].string,
                integer      : toUnbind[0].integer,
                bool         : toUnbind[0].bool,
                doub         : toUnbind[0].doub,
                date         : toUnbind[0].date,
                localDate    : toUnbind[0].localDate,
                zonedDateTime: toUnbind[0].zonedDateTime,
                instant      : toUnbind[0].instant,
        ]]
    }

    /**
     * Modify the component resolver to either find or not find a response mapper for the object.
     */
    private void hasSerializationMapper(boolean hasSerializationMapper) {
        if (hasSerializationMapper) {
            componentResolverStub.getSerializationMapper(_ as Class) >> { new DummySerializationMapper() }
        }
        else {
            componentResolverStub.getSerializationMapper(_ as Class) >> { null }
        }
    }

    static abstract class DummyParentClass {
        String parentString
    }

    static class DummyClass extends DummyParentClass implements Serializable<DummySerializationMapper> {
        String string
        Integer integer
        Boolean bool
        Double doub
        Date date
        LocalDate localDate
        ZonedDateTime zonedDateTime
        Instant instant
    }

    static class DummySerializationMapper implements SerializationMapper<DummyClass> {
        @Override
        Map<String, Object> serialize(DummyClass o) {
            // We're not testing this flow so it doesn't matter what this returns.
            return null
        }

        @Override
        Map<String, Object> serializeTabular(DummyClass o) {
            // We're not testing the mapper itself, we only need to assert that it gets invoked,
            // so it doesn't matter what this returns.
            return [test: "used SerializationMapper"]
        }
    }
}
