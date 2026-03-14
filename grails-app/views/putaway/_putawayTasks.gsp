<%@ page import="org.pih.warehouse.api.PutawayTaskStatus" %>
<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="putaway.tasks.label" default="Putaway Tasks"/>
    </h2>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th><warehouse:message code="default.actions.label" default="Actions"/></th>
            <th><warehouse:message code="putawayTask.identifier.label" default="Identifier"/></th>
            <th><warehouse:message code="putawayTask.status.label" default="Status"/></th>
            <th><warehouse:message code="putawayTask.type.label" default="Type"/></th>
            <th><warehouse:message code="putawayTask.product.label" default="Product"/></th>
            <th><warehouse:message code="putawayTask.currentLocation.label" default="Current Location"/></th>
            <th><warehouse:message code="putawayTask.container.label" default="Putaway Container"/></th>
            <th><warehouse:message code="putawayTask.destination.label" default="Destination"/></th>
            <th><warehouse:message code="putawayTask.quantity.label" default="Quantity"/></th>
            <th><warehouse:message code="putawayTask.assignee.label" default="Assignee"/></th>
            <th><warehouse:message code="putawayTask.dateStarted.label" default="Date Started"/></th>
            <th><warehouse:message code="putawayTask.dateCompleted.label" default="Date Completed"/></th>
            <th><warehouse:message code="putawayTask.dateCanceled.label" default="Date Canceled"/></th>
            <th><warehouse:message code="putawayTask.discrepancyReasonCode.label" default="Date Canceled"/></th>
            <th><warehouse:message code="receipt.receiptNumber.label" default="Receipt Number"/></th>
            <th><warehouse:message code="default.actions.label" default="Actions"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${putawayTasks}" var="task" status="i">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td class="middle" width="1%">
                    <span class="action-menu">
                        <button class="action-btn">
                            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                        </button>
                        <div class="actions">
                            <div class="action-menu-item">
                                <a href="javascript:void(0)"
                                   class="btn-edit-putaway-task"
                                   data-task-id="${task.id}">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                                    &nbsp;<warehouse:message code="putawayTask.edit.label" default="Edit Putaway Task"/>
                                </a>
                            </div>
                            <div class="action-menu-item">
                                <a href="${createLink(controller: 'putaway', action: 'putawayTaskTicket', id: task.id)}"
                                   target="_blank">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                                    &nbsp;<warehouse:message code="putawayTask.ticket.label" default="Print Ticket"/>
                                </a>
                            </div>
                        </div>
                    </span>
                </td>
                <td>${task.identifier ?: '-'}</td>
                <td>${task.status}</td>
                <td><format:metadata obj="${task.putawayTypeCode}"/></td>
                <td>
                    <g:link controller="inventoryItem" action="showStockCard" params="['product.id': task.product?.id]">
                        ${task.product?.productCode} - ${task.product?.name}
                    </g:link>
                </td>
                <td>${task.location?.name}</td>
                <td>${task.container?.name ?: '-'}</td>
                <td>${task.destination?.name ?: '-'}</td>
                <td class="right">${task.quantity}</td>
                <td>${task.assignee?.name ?: warehouse.message(code:'putawayTask.unassigned.label', default:'Unassigned')}</td>
                <td><g:formatDate date="${task.dateStarted}" format="dd/MM/yyyy"/></td>
                <td><g:formatDate date="${task.dateCompleted}" format="dd/MM/yyyy"/></td>
                <td><g:formatDate date="${task.dateCanceled}" format="dd/MM/yyyy"/></td>
                <td>${task.discrepancyReasonCode}</td>
                <td>
                    <g:if test="${task.putawayOrderItem?.receipt}">
                        <g:link controller="shipment" action="showDetails" id="${task.putawayOrderItem.receipt.shipment?.id}">
                            ${task.putawayOrderItem.receipt.receiptNumber}
                        </g:link>
                    </g:if>
                    <g:else>-</g:else>
                </td>
                <td>
                    <g:if test="${task.status == PutawayTaskStatus.PENDING}">
                        <a href="javascript:void(0)"
                           class="btn-rerun-strategy button"
                           data-task-id="${task.id}"
                           data-facility-id="${orderInstance?.destination?.id}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" />&nbsp;
                            <warehouse:message code="react.putawayTask.rerunStrategy.label" default="Rerun Strategy"/>
                        </a>
                    </g:if>
                </td>
            </tr>
        </g:each>

        <g:unless test="${putawayTasks}">
            <tr>
                <td colspan="15" class="center">
                    <warehouse:message code="putawayTask.noTasks.label" default="No tasks available"/>
                </td>
            </tr>
        </g:unless>
        </tbody>
    </table>
</div>
<script>
    $(document).on('click', '.btn-edit-putaway-task', function(e) {
        e.preventDefault();
        var taskId = $(this).data('task-id');
        editPutawayTask(taskId);
    });
</script>
<script>
    $(document).on('click', '.btn-rerun-strategy', function(e) {
        e.preventDefault();
        var taskId = $(this).data('task-id');
        var facilityId = $(this).data('facility-id');
        var confirmMessage = '${warehouse.message(code: "react.putawayTask.rerunStrategy.confirm.label", default: "Are you sure you want to rerun the putaway strategy? The current task will be replaced with new strategy results.")}';
        if (!confirm(confirmMessage)) {
            return;
        }
        var url = '${request.contextPath}/api/facilities/' + facilityId + '/putaway-tasks/' + taskId;
        $.ajax({
            url: url,
            type: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify({ action: 'rerunStrategy' }),
            success: function() {
                alert('${warehouse.message(code: "react.putawayTask.rerunStrategy.success.label", default: "Putaway strategy has been rerun successfully")}');
                window.location.reload();
            },
            error: function(xhr) {
                alert('Error: ' + (xhr.responseJSON?.message || xhr.statusText));
            }
        });
    });
</script>
