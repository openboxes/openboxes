package org.pih.warehouse.core.reflection

import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.springframework.beans.BeanUtils

class PropertyFields {

    /**
     * The kind of bean property accessor that a field has.
     */
    enum Access {
        /**
         * The field has a getter.
         */
        READ,

        /**
         * The field has a getter and a setter.
         */
        READ_WRITE,
    }

    /**
     * Finds the declared fields of a class and its superclasses, excluding any fields injected by Groovy or Grails,
     * as well as static, synthetic and transient fields, and fields that don't have the required accessors.
     *
     * @param clazz The class to collect the fields of.
     * @param stopAt The highest superclass to collect fields from (inclusive). Defaults to all superclasses.
     * @param access The accessors that a field must have to be included in the result.
     */
    static List<Field> getPropertyFields(Class clazz, Class stopAt=null, Access access=Access.READ) {
        List<Field> fields = []
        for (Class current = clazz; current != null && current != Object; current = current.superclass) {
            fields.addAll(current.declaredFields.findAll { isPropertyField(clazz, it, access) })
            if (current == stopAt) {
                break
            }
        }
        return fields
    }

    private static boolean isPropertyField(Class clazz, Field field, Access access) {
        // Synthetic fields are injected by the compiler (e.g. Groovy metaClasses), static fields aren't a part of
        // the object's data (e.g. Grails' "static constraints"), and transient fields are explicitly marked as not
        // being a part of the object's state.
        if (field.synthetic || Modifier.isStatic(field.modifiers) || Modifier.isTransient(field.modifiers)) {
            return false
        }

        /*
         * Traits inject plain private instance fields into the implementing class. For example, Grails'
         * Validateable trait adds "grails_validation_Validateable__errors" to all its implementations.
         *
         * Those injected fields have no getters or setters though, so we can filter them out that way
         * (via the property descriptor).
         */
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, field.name)
        if (!descriptor?.readMethod) {
            return false
        }

        // An optional filter for if we need the field to have a write method.
        return access == Access.READ || descriptor.writeMethod != null
    }
}
