package warehouse

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces


@Path('/api/sync')
class SyncResource {

    @GET
    @Produces('text/plain')
    String getSyncRepresentation() {
        'Sync'
    }
    
}
