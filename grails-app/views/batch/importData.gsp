
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
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
                                    <label><warehouse:message code="location.label"/></label>
                                </td>
                                <td class="value">
                                    ${commandInstance?.location}
                                </td>
                            </tr>
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
                        </table>
                    </div>

                    <g:if test="${commandInstance?.data}">
                        <div class="box">
                            <h2>${g.message(code:'default.data.label')}</h2>
                            <table id="dataTable">
                                <thead>
                                    <tr>
                                        <g:each var="column" in="${commandInstance?.columnMap?.columnMap }" status="i">
                                            <th>${column?.value}</th>
                                        </g:each>
                                    </tr>
                                </thead>
                                <tbody>
                                <g:each var="row" in="${commandInstance?.data}" status="status">
                                    <tr class="${status%2?'even':'odd' }">
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
                                    </tr>
                                </g:each>
                                </tbody>

                            </table>
                        </div>
                    </g:if>

                        <div class="buttons">
                            <g:if test="${!commandInstance?.hasErrors()}">
                                <button type="submit" name="import" value="true" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/>&nbsp;
                                ${warehouse.message(code: 'default.button.finish.label')}
                                </button>
                            </g:if>

                            <a href="${createLink(controller: "batch", action: "importData", params: params)}"
                               class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}"/>&nbsp;
                            <warehouse:message code="default.button.back.label" default="Back"/>
                            </a>


                        </div>

                    </div>
                </g:form>
            </g:if>
            <g:if test="${!commandInstance?.data}">
                <div class="box">
                    <h2><warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/></h2>
                    <div class="dialog">
                        <g:render template="uploadFileForm"/>
                    </div>
                </div>
            </g:if>


		</div>
        <script type="text/javascript">
            $(document).ready(function(){
                $('#dataTable').dataTable({
                    "bJQueryUI": true,
                    "bAutoWidth": true,
                    "sScrollX": "100%",
                    "sScrollY": 300,
                    "bScrollCollapse": true,
                    "bScrollInfinite": true,
                    "iDisplayLength": 15,
                    "bSearch": false
                });
            });

        </script>

	</body>

</html>
