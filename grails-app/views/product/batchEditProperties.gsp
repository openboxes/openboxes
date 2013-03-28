
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />

    <title><warehouse:message code="product.batchEdit.label" /></title>

</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <%--
        <g:hasErrors bean="${commandInstance}">
            <div class="errors">
                <g:renderErrors bean="${commandInstance}" as="list" />
            </div>
        </g:hasErrors>
        --%>

        <div class="yui-gf">
            <div class="yui-u first">


            </div>
            <div class="yui-u">
                <div class="box">
                    <div>

                        <h2>
                            <label>
                                <warehouse:message code="default.results.label"/>
                            </label>
                        </h2>
                    </div>
                    <div>
                        ${products.size()}
                    </div>


                </div>

            </div>
        </div>
    </div>
    <script>
        $(document).ready(function() {
            $(".chzn-select").chosen();

        });
    </script>
</body>
</html>
