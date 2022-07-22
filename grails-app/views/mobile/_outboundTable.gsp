<table class="table table-borderless table-striped">
    <thead>
        <tr>
            <th class="col-2">Order Number</th>
            <th class="col-3">Destination</th>
            <th class="col-3">Delivery for FA</th>
            <th class="col-2">Delivery Date</th>
            <th class="col-1">Status</th>
            <th class="col-1">Details</th>
        </tr>
    </thead>
    <tbody>
        <g:each var="stockMovement" in="${stockMovements}">
            <tr>
                <td>${stockMovement.identifier}</td>
                <td>${stockMovement.destination}</td>
                <td>
                    <g:if test="${stockMovement?.categories}">
                        ${stockMovement?.categories[0]?.name}
                    </g:if>
                    <g:else>
                        Not Available
                    </g:else>
                </td>
                <td>
                    <g:if test="${stockMovement?.expectedDeliveryDate}">
                        ${g.formatDate(date: stockMovement.expectedDeliveryDate, format: "dd MMM yyyy HH:mm")}
                    </g:if>
                    <g:else>
                        <i>${g.formatDate(date: stockMovement.requestedDeliveryDate, format: "dd MMM yyyy")}</i>
                    </g:else>
                </td>
                <td>${stockMovement.status}</td>
                <td><g:link controller="mobile" action="outboundDetails" id="${stockMovement?.id}" class="card-link">Details</g:link></td>
            </tr>
        </g:each>
    </tbody>
</table>
<g:unless test="${stockMovements}">
    <div class="alert alert-secondary text-muted text-center">
        There are no orders matching that criteria
    </div>
</g:unless>
