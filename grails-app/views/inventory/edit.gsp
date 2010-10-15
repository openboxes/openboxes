
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${warehouseInstance?.id}" />
                <g:hiddenField name="version" value="${warehouseInstance?.version}" />
                <div class="dialog">
					<script type="text/javascript">
					$(function() {
						$("#tabs").tabs().addClass('ui-tabs-vertical ui-helper-clearfix');
						$("#tabs li").removeClass('ui-corner-top').addClass('ui-corner-left');
					});
					</script>
					<style type="text/css">						
					/* Vertical Tabs
					----------------------------------*/
					.ui-tabs-vertical { width: 60em; }
					.ui-tabs-vertical .ui-tabs-nav { padding: .2em .1em .2em .2em; float: left; width: 15em; }
					.ui-tabs-vertical .ui-tabs-nav li { clear: left; width: 100%; border-bottom-width: 1px !important; border-right-width: 0 !important; margin: 0 -1px .2em 0; }
					.ui-tabs-vertical .ui-tabs-nav li a { display:block; }
					.ui-tabs-vertical .ui-tabs-nav li.ui-tabs-selected { padding-bottom: 0; padding-right: .1em; border-right-width: 1px; border-right-width: 1px; }
					.ui-tabs-vertical .ui-tabs-panel { padding: 1em; float: right; width: 40em;}
					</style>
					<div class="demo">
						<div id="tabs">
							<ul>
								<g:each in="${productTypes}" var="productType">
									<li><a href="#tab-${productType.id}">${productType.name}</li>
						        </g:each>        
							</ul>
							
							<g:each in="${productTypes}" var="productType">
							
								
								<div id="tab-${productType.id}">
								
									<b>${productType.name}</b>	
									<g:if test="${productMap.productType}">
										<table>	                          
											<g:each in="${productMap.productType}" var="product" status="i">
												<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
													<td>
														<g:checkBox name="includeProduct" style="vertical-align: bottom;"/> ${product?.name} 
													</td>
												</tr>
											</g:each>
										</table>
									</g:if>
								</div>
							</g:each>
						</div>
					</div>

	                <div class="buttons">
	                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
	                </div>                            	

                </div>
            </g:form>
        </div>
    </body>
</html>
