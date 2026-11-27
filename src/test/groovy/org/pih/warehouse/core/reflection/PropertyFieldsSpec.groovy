package org.pih.warehouse.core.reflection

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.validation.ObjectValidatable

@Unroll
class PropertyFieldsSpec extends Specification {

    void "getPropertyFields includes inherited properties"() {
        expect:
        assert names(PropertyFields.getPropertyFields(Child)) == ["childString", "parentString"] as Set
    }

    void "getPropertyFields stops at the given superclass (inclusive)"() {
        expect:
        assert names(PropertyFields.getPropertyFields(GrandChild, Child)) == ["grandChildString", "childString"] as Set
    }

    void "getPropertyFields excludes static, transient and trait-injected fields"() {
        expect: "the Validateable trait's errors and beforeValidateHelper fields are excluded"
        assert names(PropertyFields.getPropertyFields(WithNonDataFields)) == ["string"] as Set
    }

    void "getPropertyFields includes read-only properties for #access: #expected"() {
        expect:
        assert names(PropertyFields.getPropertyFields(WithReadOnlyProperty, null, access)) == expected as Set

        where:
        access                             || expected
        PropertyFields.Access.READ         || ["readOnly", "readWrite"]
        PropertyFields.Access.READ_WRITE   || ["readWrite"]
    }

    private static Set<String> names(List fields) {
        return fields*.name as Set
    }

    static class Parent {
        String parentString
    }

    static class Child extends Parent {
        String childString
    }

    static class GrandChild extends Child {
        String grandChildString
    }

    static class WithNonDataFields implements ObjectValidatable {
        static String STATIC_STRING = "static"
        static constraints = {
            string(nullable: true)
        }

        String string
        transient String transientString
    }

    static class WithReadOnlyProperty {
        final String readOnly = "A"
        String readWrite
    }
}
