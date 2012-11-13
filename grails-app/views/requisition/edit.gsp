
<%@ page import="org.pih.warehouse.core.RoleType" %>
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
                <div class="title" id="description">${requisition.name ?: warehouse.message(code: 'requisition.label', default: 'Requisition')}</div>
                <g:if test="${requisition.lastUpdated}">
                      <div class="time-stamp fade"><g:formatDate date="${requisition.lastUpdated }" format="dd/MMM/yyyy hh:mm a"/></div>
                </g:if>
                <div class="status fade">${requisition.status.toString()}</div>
                
                %{--${requisition?.requisitionItems?.size() }--}%
            </div>

            <g:form name="requisitionForm" method="post">
                <g:hiddenField name="id" value="${requisition?.id}" />
                <g:hiddenField name="version" value="${requisition?.version}" />
                <input type="hidden" id="name" name="name" size="80" value="${requisition.name}"/>

                <div class="dialog">
                    <table id="requisition">
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="origin.id"><warehouse:message code="requisition.requestingDepot.label" /></label>

                                </td>
                                <td colspan="5" valign="top" class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}" id="depot">
                                	<g:select name="origin.id" from="${depots}"
                                		optionKey="id" optionValue="name" value="${requisition?.origin?.id}" noSelection="['null':'']" />

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="recipientProgram"><warehouse:message code="requisition.program.label" /></label>
                                </td>

                                <td colspan="5" valign="top" class="value ${hasErrors(bean: requisition, field: 'recipientProgram', 'errors')}">
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
                                <td colspan="5" valign="top" class="value ${hasErrors(bean: requisition, field: 'requestedBy', 'errors')}">
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
                                <td colspan="5" class="value ${hasErrors(bean: requisition, field: 'dateRequested', 'errors')}">
                                    <g:jqueryDatePicker id="dateRequested" name="dateRequested"
                                        value="${requisition.dateRequested}" format="MM/dd/yyyy" maxDate="${new Date()}"/>
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label></td>
                                <td colspan="5" class="value ${hasErrors(bean: requisition, field: 'requestedDeliveryDate', 'errors')}">
                                    <g:jqueryDatePicker id="requestedDeliveryDate" name="requestedDeliveryDate"
                                        value="${requisition.requestedDeliveryDate}" format="MM/dd/yyyy" minDate="${new Date().plus(1)}"/>
                                </td>
                            </tr>



                            <g:if test="${requisition.id}">
                                <tr class="prop">
                                    <th valign="top" class="name">

                                    </th>
                                    <th class="list-header">
                                        ${warehouse.message(code: 'requisitionItem.item.label')}
                                    </th>
                                    <th class="center">
                                        ${warehouse.message(code: 'requisitionItem.quantity.label')}
                                    </th>
                                    <th class="center">
                                        ${warehouse.message(code: 'requisitionItem.substitutable.label')}
                                    </th>
                                    <th class="list-header">
                                        ${warehouse.message(code: 'requisitionItem.recipient.label')}
                                    </th>
                                    <th class="list-header">
                                        ${warehouse.message(code: 'requisitionItem.comment.label')}
                                    </th>
                                    <th class="list-header center">
                                        ${warehouse.message(code: 'requisitionItem.delete.label')}
                                    </th>
                                </tr>
                                <g:each var="requisitionItem" in="${requisition.requisitionItems}" status="i">
                                    <tr id="requisitionItemRow-${i }" class="requisitionItem ${i%2?'even':'odd' }">
                                        <g:render template="editItem" model="[requisition: requisition, requisitionItem:requisitionItem, rowIndex:requisitionItem.orderIndex]"/>
                                    </tr>
                                </g:each>
                                <g:if test="${requisition.requisitionItems == null || requisition.requisitionItems?.size() == 0}">
                                    <tr class="requisitionItem">
                                        <g:render template="editItem" model="[requisition: requisition, rowIndex: 0]" />
                                    </tr>
                                </g:if>
                            </g:if>

                            <tr class="prop">

                            	<td valign="top">
                            	</td>
                            	<td>
                            		<div class="buttons left">
		                            	<button type="button" id="addItemButton" name="addItemButton">
								            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
								            <warehouse:message code="requisitionItem.addrow.label"/>
								        </button>
								    </div>
							    </td>
							    <td colspan="4">
									<div class="buttons right">
                                        <g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
                                            <g:actionSubmit class="delete" onclick="this.form.action='${createLink(action:'delete')}';" value="Delete" />
                                        </g:isUserInRole>
                                        &nbsp;
                                        <g:actionSubmit class="save" onclick="this.form.action='${createLink(action:'save')}';" value="Save" />
										&nbsp;
										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label')}
										</g:link>
                                        &nbsp;
                                        <g:actionSubmit class="process" onclick="this.form.action='${createLink(action:'process')}';" value="Process" />
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



                $("#addItemButton").click(function() {
                    var clonedRow = $(".requisitionItem:first").clone().html();
                    var newIndex = $(".requisitionItem").length;
                    clonedRow = clonedRow.replace(/\[\d\]/g, "[" + newIndex + "]");
                    clonedRow = clonedRow.replace(/-\d-/g, "-" + newIndex + "-");
                    var cssClass = (newIndex % 2) ? "even" : "odd";
                    var appendRow = $('<tr class="requisitionItem ' + cssClass + '">' + clonedRow + '</tr>');
                    
					// Reset all input fields
                    appendRow.find("input").val("");
                    appendRow.find("input").removeAttr("checked");
                    appendRow.find(".order-index").val(newIndex);
                    $(".requisitionItem:last").after(appendRow);
                });

                function deleteRow(currentNode){
                    $(currentNode).parent().parent().parent().remove();
                }

				
                
                %{--$('.requisitionItem').live('focusout', function() {--}%
                    %{--var recipient = $(this).find('[name="recipient"]');--}%

                    %{--$.ajax({--}%
                        %{--type: "POST",--}%
                        %{--url: "${g.createLink(controller:'requisition',action:'saveRequisitionItem')}",--}%
                        %{--data: {--}%
                            %{--'recipient': recipient.val(),--}%
                            %{--'substitutable': $('.substitutable').is("checked")--}%
                        %{--},--}%
                        %{--dataType: "json",--}%
                        %{--success: function(jsonData) {--}%
                            %{--if(jsonData.success) {--}%

                            %{--} else {--}%

                            %{--}--}%
                        %{--}--}%
                    %{--});--}%
                %{--});--}%

//                $('.deleteRequisitionItem').live('click',function() {
//                    var rowLength = $('.requisitionItem').length;
//
//                    if(rowLength > 1){
//                        deleteRow(this);
//                    } else {
//                        $("#requisition tbody > tr:last").prev('tr').prev('tr').after(appendRow);
//                        deleteRow(this);
//                    }
//                });

            });
        </script>


    </body>
</html>
