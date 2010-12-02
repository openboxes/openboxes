
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
    </head>    
    <body>
    
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>
            
            <div class="dialog">
	            <g:form action="create" method="post">
                
    	        	<fieldset>
	                    <table>
	                        <tbody>                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><g:message code="product.name.label" default="Choose Type" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:radio name="type" value="drug" /> Drug &nbsp;
	                                	<g:radio name="type" value="durable" /> Equipment &nbsp;
	                                	<g:radio name="type" value="other" /> Other &nbsp;                               
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                            	<td valign="top" class="name"></td>
	                            	<td>
						                <div class="buttons">
											<button type="submit" class="positive">
												${message(code: 'default.button.next.label', default: 'Next &raquo;')}</button>
										</div>	                         
	                            	</td>
	                            </tr>
	                        </tbody>
	                    </table>
	                </fieldset>              
	            </g:form>
           	</div>
        </div>
    </body>
</html>
