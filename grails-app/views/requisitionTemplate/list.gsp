<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitions.label', default: 'Stock Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="yui-ga">
                <div class="yui-u first">


                    <div class="buttonBar">
                        <g:link class="button icon log" controller="requisitionTemplate" action="list">
                            <warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/>
                        </g:link>
                        <g:isUserAdmin>
                            <g:link class="button icon add" controller="requisitionTemplate" action="create" params="[type:'WARD_STOCK']">
                                <warehouse:message code="default.add.label" args="[warehouse.message(code:'requisitionTemplate.label').toLowerCase()]"/>
                            </g:link>
                        </g:isUserAdmin>
                    </div>

                    <g:set var="requisitions" value="${requisitions?.sort { it.status }}"/>
                    <g:set var="requisitionMap" value="${requisitions?.groupBy { it.status }}"/>
                    <div class="box">

                        <h2>${warehouse.message(code:'requisitionTemplates.label')}</h2>

                        <g:render template="list" model="[requisitions:requisitions]"/>
                    </div>

                </div>
            </div>
        </div>
    </body>
</html>
