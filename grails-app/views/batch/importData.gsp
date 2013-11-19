
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'footable.css')}" type="text/css" media="all" />
        <link rel="stylesheet" href="${resource(dir:'css',file:'footable.extra.css')}" type="text/css" media="all" />

    </head>
	<body>
		<div class="body">
	
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>


            <g:if test="${!commandInstance?.data}">
                <div class="dialog">
                    <g:render template="uploadFileForm"/>
                </div>
            </g:if>
			<g:if test="${commandInstance?.data}">
                <div class="notice">
                    <warehouse:message code="inventory.thereAreRowsIn.message"
                        args="[commandInstance.data.size(), commandInstance.filename,
                            commandInstance?.products?.size(), commandInstance?.inventoryItems?.size(),
                            commandInstance?.categories?.size()]" />
                </div>
                <table class="footable">
                    <thead>
                        <tr>
                            <th data-class="expand highlight"></th>

                            <th>Row</th>
                            <g:each var="column" in="${commandInstance?.columnMap?.columnMap }" status="i">
                                <th data-hide='${i>3?"phone,tablet":""}' ><warehouse:message code="import.${column.value}.label"/>

                                </th>
                            </g:each>
                        </tr>
                    </thead>
                    <tbody>
                        <g:if test="${commandInstance?.data?.size() > 100}">
                            <g:set var="dataList" value="${commandInstance?.data?.subList(0,100) }"/>
                        </g:if>
                        <g:each var="row" in="${dataList}" status="status">
                            <tr class="${status%2?'even':'odd' }">
                                <td></td>
                                <td>${status+1 }</td>
                                <g:each var="column" in="${commandInstance?.columnMap?.columnMap }">
                                    <td>${row[column.value] }</td>
                                </g:each>
                                <g:each var="prompt" in="${row?.prompts }">
                                    <td>
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
                <g:if test="${!commandInstance.errors.hasErrors()}">
                    <div class="buttonBar">
                        <g:form controller="batch" action="importData" method="POST">
                            <input name="location.id" type="hidden" value="${session.warehouse.id }"/>
                            <input name="type" type="hidden" value="${params.type }"/>

                            <div class="center">

                                <button type="submit" name="importNow" value="true" class="button icon approve">
                                    ${warehouse.message(code: 'default.button.import.label')}</button>

                                <a href="${createLink(controller: "batch", action: "importData", params: params)}" class="button icon trash">
                                    <warehouse:message code="default.button.cancel.label"/>
                                </a>
                            </div>
                        </g:form>
                    </div>
                </g:if>

			</g:if>
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
