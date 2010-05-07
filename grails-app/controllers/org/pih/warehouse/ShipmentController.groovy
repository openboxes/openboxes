package org.pih.warehouse

class DocumentUploadCommand {
   String shipmentId
   byte [] contents
}


class ShipmentController {
    /*
    static navigation = [
	[group:'tabs', action:'timeline', title: 'Timeline', order: 0],
	[action: 'list', title: 'All Shipments', order: 1],
	[action: 'incoming', title: 'Incoming Shipment', order: 2],
	[action: 'outgoing', title: 'Outgoing Shipments', order: 3],
	[action: 'outgoing', title: 'Recent Shipments', order: 4]
    ]*/


    def scaffold = Shipment

    def addShipmentAjax = {
	try {
	    //def newPost = postService.createPost(session.user.userId, params.content);
	    //def recentShipments = Shipment.findAllBy(session.user, [sort: 'id', order: 'desc', max: 20])
	    //render(template:"list", collection: recentShipments, var: 'shipment')
	    render { div(class:"errors", "success message") }
	}
	catch (Exception e) {
	    render { div(class:"errors", e.message) }
	}
    }

    def upload = { DocumentUploadCommand command ->
	println "$command.shipmentId"
	println "$command.contents"
	
	def shipment = Shipment.get(command.shipmentId)
	shipment.addToDocuments(new Attachment(contents: command.contents)).save(flush:true);


	redirect(action: 'show', id: command.shipmentId)
    }
    
    def form = {
        [ shipments : Shipment.list() ]
    }
    def view = {
	// pass through to "view shipment" page
    }

}
