<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <asset:stylesheet src="badge.css"/>

        <!-- Specify content to overload like global navigation links, page titles, etc. -->
        <style>
            .canceled-item { background-color: grey; }
            .dlg { display: none; }
        </style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

                <g:hiddenField id="orderId" name="orderId" value="${orderInstance?.id}"/>
                <g:render template="putawaySummary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>

                <g:hasErrors bean="${orderInstance}">
                    <div class="errors" role="alert" aria-label="error-message">
                        <g:renderErrors bean="${orderInstance}" as="list" />
                    </div>
                </g:hasErrors>

                <div class="yui-gf">

                    <div class="yui-u first">
                        <div id="details" class="box">
                            <h2>
                                <g:message code="putaway.details.label" default="Putaway Details"/>
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="putaway.putawayNumber.label" default="Putaway Number"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.orderNumber}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.status.label" /></label>
                                    </td>
                                    <td valign="top" id="status" class="value">
                                        <div class="tag tag-alert">
                                            ${orderInstance?.getDisplayStatus()}
                                        </div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="facility.label" default="Facility"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:link controller="location" action="show" id="${orderInstance?.destination?.id}">
                                            ${orderInstance?.destination?.name}
                                        </g:link>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="box">
                            <h2><g:message code="default.auditing.label"/></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.orderedBy}">
                                            <div>${orderInstance?.orderedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateOrdered}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.approvedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.approvedBy}">
                                            <div>${orderInstance?.approvedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateApproved}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.completedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.completedBy}">
                                            <div>${orderInstance?.completedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateCompleted}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.createdBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.createdBy}">
                                            <div>${orderInstance?.createdBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                            <label><warehouse:message code="default.updatedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.updatedBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.lastUpdated}"/></small>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li>
                                    <a href="${request.contextPath}/putaway/putawayTasks/${orderInstance.id}">
                                        <warehouse:message code="putawayTasks.label" default="Putaway Tasks"/>
                                    </a>
                                </li>
                            </ul>
                            <div class="loading">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="edit-putaway-task-dialog" class="dlg box">
                <!-- contents will be lazy loaded -->
            </div>
        </div>

        <script>
            $(document).ready(function() {
              applyActiveSection('inbound');

                $(".tabs").tabs({
                    cookie: {
                        expires: 1
                    },
                    ajaxOptions: {
                      error: function(xhr, status, index, anchor) {
                        // Reload the page if session has timed out
                        if (xhr.statusCode == 401) {
                          window.location.reload();
                        }
                      },
                      beforeSend: function() {
                        $('.loading').show();
                      },
                      complete: function() {
                        $(".loading").hide();
                      }
                    },
                    selected: ${params.tab ? params.tab : 0}
                });

                $("#edit-putaway-task-dialog").dialog({
                    autoOpen: false,
                    modal: true,
                    width: 800,
                    title: "${warehouse.message(code: 'putawayTask.edit.label', default: 'Edit Putaway Task')}"
                });
            });

            function editPutawayTask(taskId) {
                var url = "${request.contextPath}/putaway/putawayTaskFormDialog/" + taskId;
                $('.loading').show();
                $("#edit-putaway-task-dialog").html("Loading ...").load(url, function(response, status, xhr) {
                    $('.loading').hide();
                    if (status == "error") {
                        $.notify("Error loading putaway task", "error");
                    } else {
                        $(this).dialog("open");
                    }
                });
            }

            function reloadPutawayTasks() {
                var orderId = $("#orderId").val();
                var url = "${request.contextPath}/putaway/putawayTasks/" + orderId;
                $(".tabs .ui-tabs-panel").load(url);
            }
        </script>
    </body>
</html>
