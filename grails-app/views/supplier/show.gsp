
<%@ page import="org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'location.supplier.label', default: 'Supplier')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div>
                <div style="border-width: 5px">
                    <h2>${supplier?.name}</h2>
                    <g:each var="location" in="${supplier.locations.sort { it.name } }">
                        <div style="margin: 10px 0 10px 30px;">
                            <g:link controller="location" action="show" id="${location.id}">
                                ${location?.name}
                            </g:link>
                        </div>
                    </g:each>
                </div>
            </div>
            <div class="box">
                <div class="yui-u">
                    <div class="tabs tabs-ui">
                        <ul>
                            <li><a href="#tabs-priceHistory"><warehouse:message code="supplier.priceHistory.label" default="Price History"/></a></li>
                            <li><a href="#tabs-documents"><warehouse:message code="document.documents.label" default="Documents"/></a></li>
                        </ul>
                        <div id="tabs-priceHistory" class="ui-tabs-hide">
                            <g:render template="/supplier/priceHistory"/>
                        </div>
                        <div id="tabs-documents" class="ui-tabs-hide">
                            <g:render template="/supplier/documents"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script>
          $(document).ready(function() {
            $(".tabs").tabs({
              cookie: {
                expires: 1
              },
            });
          });
        </script>
    </body>
</html>
