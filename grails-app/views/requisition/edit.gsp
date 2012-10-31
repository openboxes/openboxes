

<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisition}">
	            <div class="errors">
	                <g:renderErrors bean="${requisition}" as="list" />
	            </div>
            </g:hasErrors>

            <div id="requisition-header">
                <div class="title"><warehouse:message code="requisition.label" /></div>
                <g:if test="${requisition.lastUpdated}">
                      <div class="time-stamp fade"><g:formatDate date="${requisition.lastUpdated }" format="dd/MMM/yyyy hh:mm a"/></div>
                </g:if>
                <div class="status fade">${requisition.status.toString()}</div>
            </div>





            <g:form method="post" action="save">
                <g:hiddenField name="id" value="${requisition?.id}" />
                <g:hiddenField name="version" value="${requisition?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="origin.id"><warehouse:message code="requisition.depot.label" /></label>

                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}" id="depot">
                                	<g:select name="origin.id" from="${org.pih.warehouse.core.Location.list()}"
                                		optionKey="id" optionValue="name" value="${requisition?.origin?.id}" noSelection="['null':'']" />

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="recipientProgram"><warehouse:message code="requisition.program.label" /></label>
                                </td>

                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'recipientProgram', 'errors')}">
                                    <g:autoSuggestString id="recipientProgram" name="recipientProgram" placeholder="Program"
                                                         jsonUrl="${request.contextPath }/json/findPrograms"
                                                         class="text"
                                                         value="${requisition?.recipientProgram}" label="${requisition?.recipientProgram}"/>
                                </td>
                            </tr>


                           <tr class="prop">
                                <td valign="top" class="name">
									<label for="requestedBy"><warehouse:message code="requisition.requestedBy.label" /></label>

                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'requestedBy', 'errors')}">
                                    %{--<g:autoSuggest id="requestedBy" name="requestedBy" jsonUrl="${request.contextPath }/json/findPersonByName"--}%
                                                        %{--class="text"--}%
                                                        %{--placeholder="Requested by"--}%

                                                        %{--value="${requisition?.requestedBy?.name}" label="${requisition?.requestedBy?.name}" />--}%
                                    <g:autoSuggest id="requestedBy" name="requestedBy" jsonUrl="${request.contextPath }/json/findPersonByName"
                                                        styleClass="text"
                                                        placeholder="Requested by"
                                                        valueId="${requisition?.requestedBy?.id}"
                                                        valueName="${requisition?.requestedBy?.name}"
                                                        postSelected="updateDescription();"/>

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="requisition.dateRequested.label"/></label></td>
                                <td class="value ${hasErrors(bean: requisition, field: 'dateRequested', 'errors')}">
                                    <g:jqueryDatePicker id="dateRequested" name="dateRequested"
                                        value="${requisition.dateRequested}" format="MM/dd/yyyy" maxDate="${new Date()}"/>
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label></td>
                                <td class="value ${hasErrors(bean: requisition, field: 'requestedDeliveryDate', 'errors')}">
                                    <g:jqueryDatePicker id="requestedDeliveryDate" name="requestedDeliveryDate"
                                        value="${requisition.requestedDeliveryDate}" format="MM/dd/yyyy" minDate="${new Date().plus(1)}"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="name"><warehouse:message code="default.description.label" /></label>
                                </td>
                                <td valign="top">
									<input type="hidden" id="name" name="name" size="80" value="${requisition.name}"/>
									<label id="description" name="name">${requisition.name}</label>
                                </td>
                            </tr>


                            <tr class="prop">

                            	<td valign="top" class="name">

                            	</td>
                            	<td>
									<div class="buttons left">
					                   <button type="submit">
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
											<warehouse:message code="default.button.save.label"/>
										</button>
										&nbsp;
										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label')}
										</g:link>
									</div>
								</td>
							</tr>

                        </tbody>
                    </table>
                </div>


            </g:form>
        </div>

        <script type="text/javascript">
            $(document).ready(function() {
                var updateDescription = function() {
                    var depot = $("#depot select option:selected").text() || "";
                    var program = $("#autosuggest-recipientProgram").val() || "";
                    var requestedBy = $("#requestedBy-suggest").val() || "";
                    var dateRequested = $("#dateRequested").val() || "";
                    var deliveryDate = $("#requestedDeliveryDate").val() || "";
                    var description = "${warehouse.message(code: 'requisition.label', default: 'Requisition')}: " + depot + " - " + program + ", " + requestedBy + " - " + dateRequested + ", " + deliveryDate;
                    $('#name').val(description);
                    $('#description').html(description);
                };
                $(".value").change(updateDescription);
                $(".autocomplete").bind('selected', updateDescription);

            });
        </script>


    </body>
</html>
