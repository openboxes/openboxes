
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'category.label', default: 'Category')}" />
        <title><warehouse:message code="categories.label" /></title>
        <style>
        .category-tree ul { margin-left: 5em; }
        .category-tree li { background-color: #f7f7f7;
            border: 1px dashed lightgrey; padding: .5em; margin: 1em;}

        </style>

    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${categoryInstance}">
				<div class="errors"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
			</g:hasErrors>


            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>

            <div class="yui-ga">
                <div class="yui-u first">
                    <div id="category-tree" class="box">
                        <h2><g:message code="categories.label"/></h2>
                        <%-- Display the category tree from the ROOT node --%>
                        <g:render template="tree" model="[category:rootCategory, level: 0]"/>
                    </div>
                </div>
            </div>
        </div>
    <script>
        $(function() {

            $('li.draggable').draggable(
                {
                    revert		: true,
                    autoSize		: false,
                    ghosting			: false,
                    onStop		: function()
                    {
                        $('li.droppable').each(
                            function()
                            {
                                this.expanded = false;
                            }
                        );
                    }
                }
            );

            $('li.droppable').droppable(
                {
                    accept: 'li.draggable',
                    tolerance: 'intersect',
                    over: function(event, ui) {
                        $( this ).addClass( "ui-state-highlight" );
                    },
                    out: function(event, ui) {
                        $( this ).removeClass( "ui-state-highlight" );
                    },
                    drop: function( event, ui ) {
                        ui.draggable.hide();
                        $( this ).removeClass( "ui-state-highlight" );
                        var child = ui.draggable.attr("id");
                        var parent = $(this).attr("id");
                        var url = "${request.contextPath}/category/move?child=" + child + "&newParent=" + parent;
                        window.location.replace(url);
                    }
                }
            );
        });
    </script>
    </body>
</html>
