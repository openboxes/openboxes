<%@ page import="org.pih.warehouse.core.Location" %>
<div>
    ${warehouse.message(code: 'location.noWardOrPharmacy.message', default:'Currently, there are no wards or pharmacies associated with ' + location.name)}
</div>
