package org.pih.warehouse.exporter

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.http.HttpSerializable
import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

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

    void "unbindData works for an object that has a ResponseMapper"() {
        given:
        List<DummyClass> toUnbind = [new DummyClass(string: "A")]

        and:
        hasResponseMapper(true)

        expect:
        assert unbinder.unbindData(toUnbind) == [[test: "used ResponseMapper"]]
    }

    void "unbindData works for an object that implements HttpSerializable"() {
        given:
        List<DummySerializableClass> toUnbind = [new DummySerializableClass(string: "A")]

        and:
        hasResponseMapper(false)

        expect:
        assert unbinder.unbindData(toUnbind) == [[test: "used HttpSerializable"]]
    }

    void "unbindData works for an object with neither a ResponseMapper nor HttpSerializable"() {
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
        hasResponseMapper(false)

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
    private void hasResponseMapper(boolean hasResponseMapper) {
        if (hasResponseMapper) {
            componentResolverStub.getResponseMapper(_ as Class) >> { new DummyResponseMapper() }
        }
        else {
            componentResolverStub.getResponseMapper(_ as Class) >> { null }
        }
    }

    static abstract class DummyParentClass {
        String parentString
    }

    static class DummyClass extends DummyParentClass {
        String string
        Integer integer
        Boolean bool
        Double doub
        Date date
        LocalDate localDate
        ZonedDateTime zonedDateTime
        Instant instant
    }

    static class DummySerializableClass extends DummyClass implements HttpSerializable {
        @Override
        Map<String, Object> asResponseBody() {
            // We're not testing this flow so it doesn't matter what this returns.
            return null
        }

        @Override
        Map<String, Object> asExportRow() {
            // This method is completely custom, so we only need to assert that it gets invoked.
            // It doesn't matter what this returns.
            return [test: "used HttpSerializable"]
        }
    }

    static class DummyResponseMapper implements ResponseMapper<DummyClass> {
        @Override
        Map<String, Object> asResponseBody(DummyClass o) {
            // We're not testing this flow so it doesn't matter what this returns.
            return null
        }

        @Override
        Map<String, Object> asExportRow(DummyClass o) {
            // We're not testing the mapper itself, we only need to assert that it gets invoked,
            // so it doesn't matter what this returns.
            return [test: "used ResponseMapper"]
        }
    }
}
