# 6. Reroute Requests To The New Installation

With your new server ready, you'll need to make sure that user requests are getting forwarded to it, otherwise requests
will continue trying to reach your old server.

Likely this will involve updating your DNS settings to point to the IP address of the newly provisioned machine, but the
exact steps required will depend on the specifics of your hosting method/provider.

!!! note
    This is where your downtime should end.
