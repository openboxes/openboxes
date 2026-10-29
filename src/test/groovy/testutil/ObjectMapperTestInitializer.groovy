package testutil

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier

import org.pih.warehouse.core.http.ObjectMapperConfigurer
import org.pih.warehouse.core.mapper.MapperComponentResolver

/**
 * Wraps the ObjectMapper (which handles serializing response objects) with additional configuration
 * that is specific to running tests.
 */
class ObjectMapperTestInitializer {

    private final MapperComponentResolver mapperComponentResolver
    private final ObjectMapper objectMapper

    ObjectMapperTestInitializer(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver
        this.objectMapper = new ObjectMapper()
    }

    /**
     * @return an ObjectMapper that is valid for use in tests.
     */
    ObjectMapper initialize() {
        // First apply the same configuration to the ObjectMapper that we do for non-test flows.
        //noinspection GroovyResultOfObjectAllocationIgnored
        new ObjectMapperConfigurer(objectMapper, mapperComponentResolver)

        // Spock Mock/Stub/Spy operations wrap objects with additional fields (such as $spock_interceptor). We ignore
        // those fields (which are prefixed with '$') when serializing to ensure they aren't included in the response.
        SimpleModule testModule = new SimpleModule()
        testModule.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                      BeanDescription beanDesc,
                                                      List<BeanPropertyWriter> beanProperties) {
                return beanProperties.findAll { !it.name.startsWith('$') }
            }
        })
        objectMapper.registerModule(testModule)

        return objectMapper
    }
}
