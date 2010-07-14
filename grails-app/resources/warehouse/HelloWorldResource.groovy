/*
 *  HelloWorldResource
 */

package warehouse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
//import javax.ws.rs.ProduceMime;
//import javax.ws.rs.ConsumeMime;

/**
 * REST Web Service
 *
 * @author meerasubbarao
 */

@Path("/api/helloWorld")
class HelloWorldResource {
	@Context
	private UriInfo context;
	
	/** Creates a new instance of HelloWorldResource */
	public HelloWorldResource() {
	}
	
	/**
	 * Retrieves representation of an instance of com.stelligent.ws.HelloWorldResource
	 * @return an instance of java.lang.String
	 */
	@GET
	//@ProduceMime("text/plain")
	public String getText() {
		//TODO return proper representation object
		throw new UnsupportedOperationException();
	}
	
	/**
	 * PUT method for updating or creating an instance of HelloWorldResource
	 * @param content representation for the resource
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@PUT
	//@ConsumeMime("text/plain")
	public void putText(String content) {
	}
}