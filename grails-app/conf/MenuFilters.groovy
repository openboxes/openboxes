import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.request.Request;

class MenuFilters {
	
	def filters = {
		insertMenu(controller:'*', action:'*') {
			before = {	
				try { 
					//request.setAttribute("shipments", Shipment.list().groupBy { it.status } )
					//request.setAttribute("orders", Order.list().groupBy { it.status() } )
					//request.setAttribute("requests", Request.list().groupBy { it.status() } )
					
					
				} catch (Exception e) { 
					log.warn("Unable to generate menu: " + e.message);
					// We don't want to let an exception kill the whole page, so we'll 
				}
			}
		}
	}
}
