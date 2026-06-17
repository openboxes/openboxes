package org.pih.warehouse.core.http

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.ResolvableSerializer
import org.grails.datastore.gorm.GormEntity
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Extends Jackson's ObjectMapper and registers a BeanSerializerModifier that applies OpenBoxes
 * mapping logic (ResponseMapper → ResponseBodyFormattable → toJson()) during Jackson serialization.
 *
 * This means writeValueAsString(obj) will:
 *  - For objects WITH a mapping: convert to a Map first, then serialize the Map with Jackson.
 *  - For fallthrough objects: serialize directly with Jackson's standard BeanSerializer, which
 *    fully respects Jackson annotations (@JsonIgnore, @JsonProperty, etc.).
 *  - For fields of fallthrough objects: apply the same logic recursively. A field whose type has
 *    a ResponseMapper will be mapped even if its parent type fell through, because Jackson calls
 *    the registered serializer for each field's type independently.
 */
@Primary  // Because we're overring ObjectMapper which is auto-pulled in by spring
@Component
class OpenBoxesObjectMapper extends ObjectMapper {

    final MapperComponentResolver mapperComponentResolver

    OpenBoxesObjectMapper(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver

        SimpleModule module = new SimpleModule("OpenBoxes")
        module.setSerializerModifier(new OpenBoxesBeanSerializerModifier(mapperComponentResolver))
        registerModule(module)
    }
}

// TODO: is ^ needed? Can't we configure the existing ObjectMapper??

/**
 * Wraps every Bean type's Jackson serializer with {@link OpenBoxesWrappingSerializer}.
 * Called once per type when Jackson first builds the serializer cache for that type.
 */
class OpenBoxesBeanSerializerModifier extends BeanSerializerModifier {

    private final MapperComponentResolver mapperComponentResolver

    OpenBoxesBeanSerializerModifier(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver
    }

    @Override
    JsonSerializer<?> modifySerializer(
            SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {

        return new OpenBoxesWrappingSerializer(mapperComponentResolver, serializer as JsonSerializer<Object>)
    }
}

/**
 * Intercepts Jackson's serialization of a single Bean-type value. If the value has an OpenBoxes
 * mapping (ResponseMapper / ResponseBodyFormattable / toJson()), it is converted to a Map and that
 * Map is serialized by Jackson (which will in turn call this wrapper for any mapped types found
 * inside the Map). If no mapping exists, the original serializer is used — preserving Jackson
 * annotations such as @JsonIgnore on fallthrough types while still applying OpenBoxes mappings
 * to any mapped-type fields those fallthrough objects may contain.
 */
class OpenBoxesWrappingSerializer extends JsonSerializer<Object> implements ResolvableSerializer, ContextualSerializer {

    private final MapperComponentResolver mapperComponentResolver
    private final JsonSerializer<Object> delegate

    // Identity-based (not equals-based) so we detect the exact same object instance being
    // re-entered on the current thread's serialization stack, which is what Hibernate
    // bidirectional associations cause.
    private static final ThreadLocal<Set<Object>> SERIALIZING_STACK =
            new ThreadLocal<Set<Object>>() {
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
        Set<Object> stack = SERIALIZING_STACK.get()
        if (!stack.add(value)) {
            // This exact object instance is already being serialized higher up in the call stack.
            // Writing null breaks the cycle rather than overflowing the stack.
            gen.writeNull()
            return
        }
        try {
            Map mappedData = applyMapping(value)
            if (mappedData != null) {
                provider.defaultSerializeValue(mappedData, gen)
            } else if (value instanceof GormEntity) {
                // GORM domain objects without an explicit ResponseMapper must never fall through
                // to Jackson's BeanSerializer. Hibernate proxies carry internal state (handler,
                // session, factory, constraint registry, etc.) that Jackson cannot serialize.
                // Add a ResponseMapper for this type to include its data in the response.
                // TODO: no this sucks. We should handle GORM/Hibernate objects!
                gen.writeNull()
            } else {
                delegate.serialize(value, gen, provider)
            }
        } finally {
            stack.remove(value)
        }
    }

    // Must be forwarded so BeanSerializer can lazily resolve its property serializers.
    @Override
    void resolve(SerializerProvider provider) throws JsonMappingException {
        if (delegate instanceof ResolvableSerializer) {
            (delegate as ResolvableSerializer).resolve(provider)
        }
    }

    // Must be forwarded so context-dependent features (@JsonView, @JsonFilter) work on
    // fallthrough types.
    @Override
    JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
            throws JsonMappingException {
        if (delegate instanceof ContextualSerializer) {
            JsonSerializer<?> contextual =
                    (delegate as ContextualSerializer).createContextual(provider, property)
            if (!contextual.is(delegate)) {
                return new OpenBoxesWrappingSerializer(mapperComponentResolver,
                        contextual as JsonSerializer<Object>)
            }
        }
        return this
    }

    private Map applyMapping(Object value) {
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
        return null
    }
}
