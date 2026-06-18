package testutil

import com.fasterxml.jackson.databind.ObjectMapper
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.core.GenericTypeResolver
import spock.lang.Specification

import org.pih.warehouse.api.BaseApiController
import org.pih.warehouse.core.file.FileNameGenerator
import org.pih.warehouse.core.http.JsonSerializer
import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * A base class for all unit tests on API Controllers.
 *
 * Note that it is not common to need to unit test a controller. This is because our controllers should be designed
 * to have minimal logic. Likely a better approach is to unit test the service layer and write an API test to test
 * the full flow that includes the controller.
 */
abstract class ApiControllerSpec<T extends BaseApiController> extends Specification implements ControllerUnitTest<T> {

    @Override
    Class<T> getTypeUnderTest() {
        // For whatever reason, Grails only looks at the direct parent when resolving generics for ControllerUnitTest
        // (see ParameterizedGrailsUnitTest.getTypeUnderTest). So for this base class structure to work, we need to
        // tell Grails what controller is being tested.
        return (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), ApiControllerSpec.class)
    }

    void setup() {
        // Sets up the ResponseMapper components that the spec defines to be used when serializing JSON responses.
        MapperComponentResolver mapperComponentResolver = new MapperComponentResolver(
                Optional.of(setupResponseMappers()),
                Optional.empty())

        // Enables controllers to serialize their response objects to JSON during tests.
        // You can access the result of this via Grails' "response.json" convenience param.
        ObjectMapper objectMapper = new ObjectMapperTestInitializer(mapperComponentResolver).initialize()
        controller.jsonSerializer = new JsonSerializer(objectMapper)

        controller.fileNameGenerator = Stub(FileNameGenerator)
    }

    /**
     * Meant to be overwritten.
     *
     * Returns the list of response mappers to use when serializing controller responses during tests.
     *
     * If a mapper is not specified, any controller that would have used it will instead fall back to other
     * serialization methods (see BaseController for details).
     *
     * We've opted to require you to manually specify the mappers (instead of autowiring them in for tests) to give
     * us more control of the exact behaviour that we want to test.
     */
    List<ResponseMapper> setupResponseMappers() {
        return []
    }
}
