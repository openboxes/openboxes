<%@ page import="org.pih.warehouse.api.PutawayTaskStatus" %>
<style>
    .task-summary {
        background-color: #f7f7f7;
        border: 1px solid #ddd;
        border-radius: 4px;
        padding: 10px 15px;
        margin-bottom: 15px;
    }
    .task-summary h3 {
        font-size: 13px;
        margin: 0 0 8px 0;
        color: #555;
        border-bottom: 1px solid #ddd;
        padding-bottom: 5px;
    }
    .task-summary table {
        width: 100%;
        margin: 0;
    }
    .task-summary table td {
        padding: 3px 8px;
        border: none;
        font-size: 12px;
    }
    .task-summary table td.name {
        font-weight: bold;
        color: #666;
        width: 130px;
        white-space: nowrap;
    }
    .task-summary table td.value {
        color: #333;
    }
</style>
<g:form name="editPutawayTaskForm" method="post">
    <g:hiddenField id="dlgTaskId" name="task.id" value="${task?.id}"/>

    <div class="task-summary">
        <h3><warehouse:message code="putawayTask.summary.label" default="Task Summary"/></h3>
        <table>
            <tr>
                <td class="name">
                    <warehouse:message code="putawayTask.identifier.label" default="Identifier"/>
                </td>
                <td class="value">${task?.identifier ?: '-'}</td>
                <td class="name">
                    <warehouse:message code="putawayTask.quantity.label" default="Quantity"/>
                </td>
                <td class="value">${task?.quantity}</td>
            </tr>
            <tr>
                <td class="name">
                    <warehouse:message code="putawayTask.type.label" default="Type"/>
                </td>
                <td class="value" colspan="3"><format:metadata obj="${task?.putawayTypeCode}"/></td>
            </tr>
            <tr>
                <td class="name">
                    <warehouse:message code="putawayTask.product.label" default="Product"/>
                </td>
                <td class="value" colspan="3">${task?.product?.productCode} - ${task?.product?.name}</td>
            </tr>
            <tr>
                <td class="name">
                    <warehouse:message code="putawayTask.currentLocation.label" default="Current Location"/>
                </td>
                <td class="value" colspan="3">${task?.location?.name}</td>
            </tr>
            <g:if test="${task?.putawayOrderItem?.receipt}">
                <tr>
                    <td class="name">
                        <warehouse:message code="receipt.receiptNumber.label" default="Receipt Number"/>
                    </td>
                    <td class="value" colspan="3">
                        <a href="${createLink(controller: 'shipment', action: 'showDetails', id: task.putawayOrderItem.receipt.shipment?.id)}"
                           target="_blank">
                            ${task.putawayOrderItem.receipt.receiptNumber}
                        </a>
                    </td>
                </tr>
            </g:if>
        </table>
    </div>

    <table>
        <tbody>
        <tr class="prop">
            <td valign="top" class="name">
                <label for="dlgStatus"><warehouse:message code="putawayTask.status.label" default="Status"/></label>
            </td>
            <td valign="top" class="value">
                <g:select id="dlgStatus" name="status"
                          from="${PutawayTaskStatus.values()}"
                          value="${task?.status}"
                          class="select2"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label for="dlgDestination"><warehouse:message code="putawayTask.destination.label" default="Destination"/></label>
            </td>
            <td valign="top" class="value">
                <g:selectInternalLocation id="dlgDestination"
                                          name="destination.id"
                                          value="${task?.destination?.id}"
                                          from="${facility?.internalLocations?.sort { it.name }}"
                                          class="select2"
                                          noSelection="['':'']"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label for="dlgContainer"><warehouse:message code="putawayTask.container.label" default="Putaway Container"/></label>
            </td>
            <td valign="top" class="value">
                <g:selectInternalLocation id="dlgContainer"
                                          name="container.id"
                                          value="${task?.container?.id}"
                                          from="${facility?.internalLocations?.sort { it.name }}"
                                          class="select2"
                                          noSelection="['':'']"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label for="dlgAssignee"><warehouse:message code="putawayTask.assignee.label" default="Assignee"/></label>
            </td>
            <td valign="top" class="value">
                <g:selectPerson id="dlgAssignee"
                                name="assignee.id"
                                value="${task?.assignee?.id}"
                                noSelection="['':'']"
                                class="select2"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label><warehouse:message code="putawayTask.dateStarted.label" default="Date Started"/></label>
            </td>
            <td valign="top" class="value">
                <g:datePicker name="dateStarted"
                              value="${task?.dateStarted}"
                              precision="minute"
                              default="none"
                              noSelection="['':'']"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label><warehouse:message code="putawayTask.dateCompleted.label" default="Date Completed"/></label>
            </td>
            <td valign="top" class="value">
                <g:datePicker name="dateCompleted"
                              value="${task?.dateCompleted}"
                              precision="minute"
                              default="none"
                              noSelection="['':'']"/>
            </td>
        </tr>
        </tbody>
        <tfoot>
        <tr class="prop">
            <td></td>
            <td valign="top" class="value">
                <button class="button save-putaway-task-button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
                    <warehouse:message code="default.button.save.label"/>
                </button>
                <div class="right">
                    <button class="button close-putaway-task-dialog-button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_cross.png')}" />&nbsp;
                        <warehouse:message code="default.button.close.label"/>
                    </button>
                </div>
            </td>
        </tr>
        </tfoot>
    </table>
</g:form>
<script>
    function savePutawayTaskDialog() {
        var data = $("#editPutawayTaskForm").serialize();
        $.ajax({
            url: "${g.createLink(controller: 'putaway', action: 'savePutawayTask')}",
            data: data,
            success: function () {
                $.notify("${warehouse.message(code: 'putawayTask.saved.label', default: 'Saved putaway task successfully')}", "success");
                $("#edit-putaway-task-dialog").dialog("close");
                reloadPutawayTasks();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.responseText) {
                    try {
                        var data = JSON.parse(jqXHR.responseText);
                        $.notify(data.errorMessage, "error");
                    } catch (e) {
                        $.notify(jqXHR.responseText, "error");
                    }
                } else {
                    $.notify("An error occurred", "error");
                }
            }
        });
        return false;
    }

    $(document).ready(function() {
        $(".close-putaway-task-dialog-button").click(function(event) {
            event.preventDefault();
            $("#edit-putaway-task-dialog").dialog("close");
        });

        $(".save-putaway-task-button").click(function(event) {
            event.preventDefault();
            savePutawayTaskDialog();
        });

        $(".select2").select2({
            placeholder: '',
            width: '100%',
            allowClear: true
        });
    });
</script>
