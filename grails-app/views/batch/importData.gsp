
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'footable.css')}" type="text/css" media="all" />

    </head>
	<body>
		<div class="body">
	
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${commandInstance}">
				<div class="errors">
                    <g:renderErrors bean="${commandInstance}" as="list" />
                </div>
			</g:hasErrors>


            <g:if test="${commandInstance?.data}">

                <g:form controller="batch" action="importData" method="POST">
                    <input name="location.id" type="hidden" value="${session.warehouse.id }"/>
                    <input name="type" type="hidden" value="${params.type }"/>

                    <div class="box">
                        <h2><warehouse:message code="default.import.label" args="[warehouse.message(code:'default.properties.label', default:'properties')]"/></h2>
                        <table>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="default.type.label"/></label>
                                </td>
                                <td class="value">
                                    ${commandInstance?.type}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="import.filename.label" default="Filename"/></label>
                                </td>
                                <td class="value">
                                    ${commandInstance?.filename}
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="default.date.label"/></label>
                                </td>
                                <td class="value">
                                    <g:jqueryDatePicker id="date" name="date" value="${commandInstance?.date}" format="MM/dd/yyyy" size="20"/>
                                </td>

                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="location.label"/></label>
                                </td>
                                <td class="value">
                                    ${commandInstance?.location}
                                </td>

                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="default.rows.label" default="Rows"/></label>
                                </td>
                                <td class="value">
                                    ${commandInstance?.data?.size()} rows
                                </td>

                            </tr>
                            <tfoot>
                                <tr>
                                    <td colspan="2" class="center">

                                        <div class="center">
                                            <%--
                                            <button type="submit" name="validate" class="button icon approve">
                                                ${warehouse.message(code: 'default.button.validate.label', default: 'Re-validate')}</button>
                                            --%>

                                            <g:if test="${!commandInstance?.hasErrors()}">
                                                <button type="submit" name="import" value="true" class="button icon approve">
                                                    ${warehouse.message(code: 'default.button.finish.label')}</button>
                                            </g:if>
                                            <a href="${createLink(controller: "batch", action: "importData", params: params)}" class="button icon trash">
                                                <warehouse:message code="default.button.cancel.label" default="Cancel"/>
                                            </a>

                                        </div>

                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </g:form>
            </g:if>

            <div class="box">
                <h2><warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/></h2>
                <g:if test="${!commandInstance?.data}">
                    <div class="dialog">
                        <g:render template="uploadFileForm"/>
                    </div>
                </g:if>
                <g:if test="${commandInstance?.data}">

                    <table class="footable">
                        <thead>
                            <tr>
                                <th data-class="expand"></th>

                                <th>Row</th>
                                <g:each var="column" in="${commandInstance?.columnMap?.columnMap }" status="i">
                                    <th data-hide='${i>3?"phone,tablet":""}' ><warehouse:message code="import.${column.value}.label"/>

                                    </th>
                                </g:each>
                                <th>Warnings</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="row" in="${commandInstance?.data}" status="status">
                                <tr class="${status%2?'even':'odd' }">
                                    <td></td>
                                    <td class="center">${status+2 }</td>
                                    <g:each var="column" in="${commandInstance?.columnMap?.columnMap }">
                                        <td>${row[column.value] }</td>
                                    </g:each>
                                    <g:each var="prompt" in="${row?.prompts }">
                                        <td class="center">
                                            <select name="${prompt.key }">
                                                <g:each var="value" in="${prompt.value }">
                                                    <option value="${value.id }">${value.name }</option>
                                                </g:each>
                                            </select>
                                        </td>
                                    </g:each>
                                    <td>
                                        <g:if test='${!commandInstance?.warnings[status]}'>
                                            <g:each var="warning" in="${commandInstance?.warnings[status]}">
                                                <li>&bull;${warning}</li>
                                            </g:each>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>

                    </table>
                </g:if>
            </div>
		</div>
        <script src="${createLinkTo(dir:'js/footable/', file:'footable.js')}" type="text/javascript" ></script>
        <script type="text/javascript">
            $(function() {
                $('table tbody tr td code').addClass('footable-toggle');
                $('table').footable();
            });
        </script>

	</body>

</html>
