package org.pih.warehouse.admin

import org.pih.warehouse.product.Product;
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.AuthConfig
import static groovyx.net.http.ContentType.JSON
import net.sf.json.JSONObject;

class SyncController {

	
	def index = {
		def remoteProducts = new ArrayList<Product>();
		try {
			def http = new HTTPBuilder("http://localhost:8080");
			http.auth.basic("manager", "password")
			http.request(Method.valueOf("GET"), JSON) {
				uri.path = '/warehouse/api/products'
				response.success = { resp, json ->						
					json.products.each { product ->
						log.info "Product JSON: " + product						
						remoteProducts.add(JSONObject.toBean(product));
					}				
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		[ remoteProducts : remoteProducts, localProducts : Product.getAll() ]
	}
}
