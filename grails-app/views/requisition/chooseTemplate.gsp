<%@ page import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${requisition}">
		<div class="errors">
			<g:renderErrors bean="${requisition}" as="list" />
		</div>
	</g:hasErrors>	

	

	<g:render template="summary" model="[requisition:requisition]"/>

	<div class="buttonBar">
		<g:link class="button icon log" controller="requisitionTemplate" action="list">
			<warehouse:message code="default.list.label" args="[g.message(code:'requisitionTemplates.label').toLowerCase()]"/>
		</g:link>
		<g:link class="button icon add" controller="requisitionTemplate" action="create" params="[type:'STOCK']">
			<warehouse:message code="default.add.label" args="[g.message(code:'requisitionTemplate.label')]"/>
		</g:link>
	</div>


	<div class="yui-ga">
		<div class="yui-u first">


            <g:form controller="requisition" action="createStockFromTemplate">
				<g:if test="${requisition?.id }">
					<div class="box">
						<a class="toggle" href="javascript:void(0);">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
						</a>
						<h3 style="display: inline" class="toggle">${requisition?.requestNumber } ${requisition?.name }</h3>				
						<g:if test="${requisition?.id }">
							<g:if test="${!params.editHeader }">
								<g:link controller="requisition" action="editHeader" id="${requisition?.id }">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: bottom;"/>
								</g:link>
							</g:if>
							<g:else>
								<g:link controller="requisition" action="edit" id="${requisition?.id }">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}" style="vertical-align: bottom;"/>
								</g:link>									
							</g:else>
						</g:if>
					</div>
				</g:if>
                <div id="requisition-template-details" class="dialog ui-validation box">
                    <h2>${warehouse.message(code:'requisition.chooseTemplate.label', default:'Choose stock requisition template')}</h2>
                    <table id="requisition-template-table">
                        <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label for="requisitionTemplate">
                                        <warehouse:message code="requisitionTemplate.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:selectRequisitionTemplate id="requisitionTemplate" name="id"
																 noSelection="['null':'']" class="chzn-select-deselect"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
			</g:form>
		</div>
	</div>
<script>
    $("#requisitionTemplate").change(function() {
        $(this).parents("form").submit();
    });
</script>

</body>
</html>
