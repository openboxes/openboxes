<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="putaway.tasks.label" default="Putaway Tasks"/>
    </h2>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th><warehouse:message code="putawayTask.identifier.label" default="Identifier"/></th>
            <th><warehouse:message code="putawayTask.status.label" default="Status"/></th>
            <th><warehouse:message code="putawayTask.product.label" default="Product"/></th>
            <th><warehouse:message code="putawayTask.currentLocation.label" default="Current Location"/></th>
            <th><warehouse:message code="putawayTask.container.label" default="Putaway Container"/></th>
            <th><warehouse:message code="putawayTask.destination.label" default="Destination"/></th>
            <th><warehouse:message code="putawayTask.quantity.label" default="Quantity"/></th>
            <th><warehouse:message code="putawayTask.assignee.label" default="Assignee"/></th>
            <th><warehouse:message code="putawayTask.dateStarted.label" default="Date Started"/></th>
            <th><warehouse:message code="putawayTask.dateCompleted.label" default="Date Completed"/></th>
            <th><warehouse:message code="putawayTask.dateCanceled.label" default="Date Canceled"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${putawayTasks}" var="task" status="i">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td>${task.identifier ?: '-'}</td>
                <td>${task.status}</td>
                <td>${task.product?.productCode} - ${task.product?.name}</td>
                <td>${task.location?.name}</td>
                <td>${task.container?.name ?: '-'}</td>
                <td>${task.destination?.name ?: '-'}</td>
                <td class="right">${task.quantity}</td>
                <td>${task.assignee?.name ?: warehouse.message(code:'putawayTask.unassigned.label', default:'Unassigned')}</td>
                <td><g:formatDate date="${task.dateStarted}" format="dd/MM/yyyy"/></td>
                <td><g:formatDate date="${task.dateCompleted}" format="dd/MM/yyyy"/></td>
                <td><g:formatDate date="${task.dateCanceled}" format="dd/MM/yyyy"/></td>
            </tr>
        </g:each>

        <g:unless test="${putawayTasks}">
            <tr>
                <td colspan="11" class="center">
                    <warehouse:message code="putawayTask.noTasks.label" default="No tasks available"/>
                </td>
            </tr>
        </g:unless>
        </tbody>
    </table>
</div>
