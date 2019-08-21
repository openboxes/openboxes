
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="category.productCategories.label" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${categoryInstance}">
				<div class="errors"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
			</g:hasErrors>


            <div class="buttonBar">
                <g:link class="button" controller="category" action="tree"><warehouse:message code="default.list.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
                <g:isUserAdmin>
                    <g:link class="button" controller="category" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
                </g:isUserAdmin>
            </div>

            <div class="yui-ga">
                <div class="yui-u first">
                    <table style="width:auto;">
                        <tr>
                            <td>
                                <fieldset>
                                    <div>
                                        <style>
                                        .category-tree ul { margin-left: 2em; }
                                        .category-tree li { background-color: #f7f7f7;
                                            border: 1px dashed lightgrey; padding: .5em; margin: .5em;}
                                        </style>

                                        <%-- Display the category tree from the ROOT node --%>
                                        <g:render template="tree" model="[category:rootCategory, level: 0]"/>


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
                                    </div>
                                </fieldset>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </body>
</html>
