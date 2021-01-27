package org

import grails.gorm.transactions.Rollback
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor
import grails.testing.mixin.integration.Integration
import grails.web.servlet.mvc.GrailsHttpSession
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import org.grails.web.json.JSONObject
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.pih.warehouse.SecurityInterceptor
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.user.AuthController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import io.micronaut.core.type.Argument

import javax.servlet.http.HttpSession

//@TestFor(AuthController)
@Integration
@Rollback
class ApiControllerIntegrationTestSpec extends Specification {

    @Shared HttpClient client

//    @Autowired
    @Shared
    def securityInterceptor;

    @Shared
    def authController

    def setup() {
        String baseUrl = "http://localhost:$serverPort/openboxes"
        println "baseUrl::${baseUrl}"
        def session = Mock(HttpSession)
        securityInterceptor = Mock(SecurityInterceptor)
        authController = Mock(AuthController) as AuthController
        authController.session = session
        securityInterceptor.session = session
        authController.handleLogin()
        this.client = HttpClient.create(baseUrl.toURL())
//        String urlParameters  = "targetUri=&browserTimezone=Asia%2FKolkata&username=admin&password=password";
//        byte[] postData       = urlParameters.getBytes();
//        int    postDataLength = postData.length;
//        URL    url            = new URL( baseUrl );
//        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//        conn.setDoOutput( true );
//        conn.setInstanceFollowRedirects( false );
//        conn.setRequestMethod( "POST" );
//        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty( "charset", "utf-8");
//        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//        conn.setUseCaches( false );
//        try {
//            conn.getOutputStream().write(postData);
//
//            Reader inn = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//
//            for (int c; (c = inn.read()) >= 0;)
//                System.out.print((char)c);
//        }catch(Exception ex){
//println "Ex: ${ex.printStackTrace()}"
//        }
//        MockHttpSession session = new MockHttpSession();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()
        form.add("username", "admin")
        form.add("password", "password")
        def response = new RestBuilder().post("http://localhost:$serverPort/openboxes/auth/handleLogin") {
            contentType("application/x-www-form-urlencoded")
            body(form)

        }
        println "session>>::${session.user}"
//        URL url = new URL("http://localhost:$serverPort/openboxes/auth/handleLogin");
//
//        Map<String,Object> params = new LinkedHashMap<>();
//        params.put("username", "admin");
//        params.put("password", "password");
//        params.put("browserTimezone", "Asia/Kolkata");
//
//        StringBuilder postData = new StringBuilder();
//        for (Map.Entry<String,Object> param : params.entrySet()) {
//            if (postData.length() != 0) postData.append('&');
//            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
//            postData.append('=');
//            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
//        }
//        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
//
//        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//        conn.setDoOutput(true);
//        conn.getOutputStream().write(postDataBytes);
//
//        Reader inn = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//
//        for (int c; (c = inn.read()) >= 0;)
//            System.out.print((char)c);
//    }
    }

    def cleanup() {
    }

    void "test product list"() {
//        given:
//
//        def session = Mock(GrailsHttpSession)
//        def request = Mock(GrailsWebRequest)
//        request.session = session
        def userInstance = User.findByUsernameOrEmail("admin", "admin")
//        session.user = userInstance
        LocationType defaultLocationType = LocationType.findByLocationTypeCode(LocationTypeCode.DISPENSARY)
        Location location = new Location(name: "XYZ Location", locationType: defaultLocationType)
        location.save()
        println "location:::${location?.id}"

        when:
////        HttpRequest request = HttpRequest.GET("/generic/product")
////        println "Before exchange"
////        HttpResponse<List<Map>> resp = client.toBlocking().exchange(request, Argument.of(List, Map))
////        println "resp::${resp}"
//        request.sess
        URL url = new URL("http://localhost:$serverPort/openboxes/api/generic/product");
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        Reader inn = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

//        for (int c; (c = inn.read()) >= 0;)
//            System.out.print((char)c);


//        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("http://localhost:$serverPort/openboxes/api/generic/product")
//                .session(session);
//
//        then:
//        true == true
////        resp.status == HttpStatus.OK
////        resp.body()

//        given:
//        println "start"
////        1 * mockedArrivalRepository.registerArrival(_) >> {
////            Optional.empty()
////        }
//
//        when:
//        def response = new RestBuilder().post("http://localhost:${serverPort}/api/arrivals") {
//            json {
//                animalId = 1
//                date = '2017-01-01'
//            }
//        }
//
        then:
        200 == 200

    }
}
