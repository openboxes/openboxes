package org.pih.warehouse.admin

import org.pih.warehouse.product.Product;
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.AuthConfig
import static groovyx.net.http.ContentType.JSON
import net.sf.json.JSONObject;
import net.sf.json.JSONException;

class SyncController {

	
	def index = {
		def remoteProducts = new ArrayList<Product>();
		def http = new HTTPBuilder("http://ci.pih-emr.org:8080");
		http.auth.basic("manager", "password")
		http.request(Method.valueOf("GET"), JSON) {
			uri.path = '/warehouse/api/products'
			response.success = { resp, json ->						
				json.products.each { product ->					
					try { 
						log.info "Product JSON: " + product
						remoteProducts.add(JSONObject.toBean(product, Product.class));
					} 
					catch (JSONException e) {
						log.error("Unable to pull products from remote server", e);
						remoteProducts.add(JSONObject.toBean(product));
					}
			
											
					
				}				
			}
		} 
		
		[ remoteProducts : remoteProducts, localProducts : Product.getAll() ]
	}
}
