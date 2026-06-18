package org.pih.warehouse.core.http

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.ResolvableSerializer
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module
import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Configures Jackson's ObjectMapper, which handles API serialization.
 *
 * Note that by default Grails does not use the ObjectMapper (it uses {@link grails.converters.JSON} instead).
 * As such, this serialization will only be triggered on Controllers if opted in to, which is the case for
 * any controller that implements {@link org.pih.warehouse.core.BaseController}.
 */
@Component
class ObjectMapperConfigurer {

    ObjectMapperConfigurer(final ObjectMapper objectMapper,
                               final MapperComponentResolver mapperComponentResolver) {

        // Prevent getter and boolean is* methods from being automatically serialized as fields.
        // You can still serialize individual getters by annotating them with @JsonProperty.
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)

        // Groovy makes the fields themselves "private" (because it auto-generates getters) so
        // we need to set field visibility to ANY to be able to auto-serialize fields.
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        // If an object being serialized has no fields to serialize, don't throw an error,
        // just ignore the object. This gracefully resolves Grails/GORM AST transformation weirdness.
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

        // Add support for serializing Hibernate domain entities (though we should typically
        // prefer converting an entity to a DTO and serializing the DTO instead).
        objectMapper.registerModule(new Hibernate5Module())

        // Instruct the ObjectMapper to consult our custom module whenever it constructs a serializer.
        SimpleModule module = new SimpleModule("OpenBoxes")
        module.setSerializerModifier(new OpenBoxesBeanSerializerModifier(mapperComponentResolver))
        objectMapper.registerModule(module)
    }
}

/**
 * Modifies the serialization process, wrapping it with the custom behaviour defined in OpenBoxesWrappingSerializer.
 *
 * Note that this flow will not impact primitives, Lists, Maps, or other types that have their own built-in serializers.
 * Those serializers are defined and handled explicitly by Jackson so we don't need to worry about them.
 *
 * During serialization, when Jackson first encounters a type that it hasn't serialized before, Jackson's
 * BeanSerializerFactory builds a new serializer for that type and caches it for when it sees that type again.
 * At the end of that process, the BeanSerializerFactory iterates over each BeanSerializerModifier, calling
 * modifySerializer to extend/wrap it with the specified behaviour.
 */
class OpenBoxesBeanSerializerModifier extends BeanSerializerModifier {

    private final MapperComponentResolver mapperComponentResolver

    OpenBoxesBeanSerializerModifier(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver
    }

    @Override
    JsonSerializer<?> modifySerializer(SerializationConfig config,
                                       BeanDescription beanDesc,
                                       JsonSerializer<?> serializer) {

        // Wraps the original serializer that Jackson generated with our OpenBoxesWrappingSerializer.
        // The serializer will be cached after this point, so the overhead of wrapping the serializer should
        // only apply the first time a type is serialized (at least until the next application restart).
        return new OpenBoxesWrappingSerializer(mapperComponentResolver, serializer as JsonSerializer<Object>)
    }
}

/**
 * Wraps the default serialization process to check for our custom defined behaviours.
 *
 * These include (in priority order):
 *
 * 1) defining a ResponseMapper for the object
 * 2) implementing ResponseBodyFormattable
 * 3) implementing a toJson() method
 *
 * If any object being serialized has one of the above, it will be serialized via that method, otherwise it
 * will serialize via Jackson's default behaviour.
 *
 * Any object being serialized via one of the above methods will ignore any Jackson annotations on the object.
 *
 * This works at any level in the object hierarchy. If a DTO contains a field that implements one of the above
 * custom serialization methods, that field will be serialized via that method.
 */
class OpenBoxesWrappingSerializer extends JsonSerializer<Object> implements ResolvableSerializer, ContextualSerializer {

    private final MapperComponentResolver mapperComponentResolver

    /**
     * The BeanSerializer holding the default serialization behaviour that Jackson would perform.
     */
    private final JsonSerializer<Object> delegate

    /**
     * A thread-safe set of object identities used to check for circular references when serializing.
     */
    private static final ThreadLocal<Set<Object>> CIRCULAR_REFERENCE_STACK = new ThreadLocal<Set<Object>>() {
        @Override
        protected Set<Object> initialValue() {
            return Collections.newSetFromMap(new IdentityHashMap<>())
        }
    }

    OpenBoxesWrappingSerializer(final MapperComponentResolver mapperComponentResolver,
                                final JsonSerializer<Object> delegate) {
        this.mapperComponentResolver = mapperComponentResolver
        this.delegate = delegate
    }

    @Override
    void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(gen)
            return
        }

        // If the circular reference stack already contains the object, we have a circular reference.
        Set<Object> stack = CIRCULAR_REFERENCE_STACK.get()
        if (!stack.add(value)) {
            handleCircularReference(value, gen)
            return
        }

        try {
            Map mappedData = applyCustomMapping(value)
            if (mappedData != null) {
                provider.defaultSerializeValue(mappedData, gen)
            } else {
                // If our custom serializing did nothing, let Jackson serialize the object.
                delegate.serialize(value, gen, provider)
            }
        } finally {
            // We've serialized all of the object's fields, so remove it from the stack as we bubble back up
            // to the object's parent.
            stack.remove(value)
        }
    }

    @Override
    void resolve(SerializerProvider provider) throws JsonMappingException {
        // Preserves default behaviour for non-custom serialized objects.
        // Allows BeanSerializer to lazily resolve its property serializers.
        if (delegate instanceof ResolvableSerializer) {
            (delegate as ResolvableSerializer).resolve(provider)
        }
    }

    @Override
    JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        // Allows context-dependent features (such as @JsonView, @JsonFilter) to work.
        if (delegate instanceof ContextualSerializer) {
            JsonSerializer<?> contextual = (delegate as ContextualSerializer).createContextual(provider, property)
            if (!contextual.is(delegate)) {
                return new OpenBoxesWrappingSerializer(mapperComponentResolver, contextual as JsonSerializer<Object>)
            }
        }
        return this
    }

    private void handleCircularReference(Object value, JsonGenerator gen) {
        // This exact object instance is already being serialized higher up in the call stack.
        // Writing null breaks the cycle rather than overflowing the stack.
        if (value.hasProperty("id")) {
            try {
                gen.writeString(value.id as String)
                return
            } catch (Exception ignore) {
                // Do nothing. We'll just return null if the id field cannot be stringified for whatever reason.
            }
        }
        gen.writeNull()
    }

    /**
     * Performs our custom serialization on the given object if one of the following is true:
     *
     * 1) A ResponseMapper is defined for the object
     * 2) The object implements ResponseBodyFormattable
     * 3) The object defines a toJson() method
     */
    private Map applyCustomMapping(Object value) {
        ResponseMapper responseMapper = mapperComponentResolver.getResponseMapper(value.class)
        if (responseMapper) {
            return responseMapper.asResponseBody(value)
        }
        if (value instanceof ResponseBodyFormattable) {
            return value.asResponseBody()
        }
        if (value.metaClass.respondsTo(value, "toJson")) {
            return value.toJson()
        }

        // We don't know how to serialize the object so we will rely on the framework to do it for us.
        return null
    }
}
