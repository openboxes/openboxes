
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
    </head>
    <body>
        <div class="body">
        
        	<div class="nav">
				<g:render template="nav"/>        	
        	</div>
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" action="update">
            	<fieldset>
            		
	                <g:hiddenField name="id" value="${warehouseInstance?.id}" />
	                <g:hiddenField name="version" value="${warehouseInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                    	<thead>
								<tr>
				        			<td valign="top" colspan="2">
										<g:render template="summary"/>			            			
									</td>            
		                    	</tr>	                    	
	                    	</thead>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><warehouse:message code="warehouse.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${warehouseInstance?.name}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="bgColor"><warehouse:message code="warehouse.bgColor.label" default="Background Color" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'bgColor', 'errors')}">
	                                    
	                                    <g:select name="bgColor" class="colorpicker" 
	                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
	                                    	value="${warehouseInstance?.bgColor}" />
	                                    
                                    	<span class="fade">${warehouseInstance?.bgColor }</span>
	                              
	                                    
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="fgColor"><warehouse:message code="warehouse.fgColor.label" default="Foreground Color" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'fgColor', 'errors')}">
	                                    <g:select name="fgColor" class="colorpicker" 
	                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
	                                    	value="${warehouseInstance?.fgColor}" />
	                                    	
                                    	<span class="fade">${warehouseInstance?.fgColor }</span>
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><warehouse:message code="warehouse.manager.label" default="Manager" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'manager', 'errors')}">
	                                    <g:select name="manager.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${warehouseInstance?.manager?.id}"  />
	                                </td>
	                            </tr>
	                            
	                            <!--  
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="parentLocation"><warehouse:message code="warehouse.parentWarehouse.label" default="Parent Location" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'parentLocation', 'errors')}">
										<g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}" 
											optionKey="id" optionValue="name" value="" noSelection="['null': '']" />							
	                                </td>
	                            </tr>
	                            -->
	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><warehouse:message code="warehouse.manager.label" default="Managed Locally" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'local', 'errors')}">
	                                    <g:checkBox name="local" value="${warehouseInstance?.local}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><warehouse:message code="warehouse.manager.label" default="Active" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'active', 'errors')}">
	                                    <g:checkBox name="active" value="${warehouseInstance?.active}" />
	                                </td>
	                            </tr>
	                            <tr>
	                            	<td valign="top"></td>
	                            	<td valign="top">
					                   <button type="submit">								
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;Save
										</button>
										&nbsp;
										<g:link action="list">
											${message(code: 'default.button.cancel.label', default: 'Cancel')}						
										</g:link>			
									</td>
	                            </tr>
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
	    <script type="text/javascript">

	        function selectCombo(comboBoxElem, value) {
		        alert(comboBoxElem + " " + value)
				if (comboBoxElem != null) {
					if (comboBoxElem.options) { 
						for (var i = 0; i < comboBoxElem.options.length; i++) {
				        	if (comboBoxElem.options[i].value == value &&
				                comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
				                comboBoxElem.options[i].selected = true;
				                break
				        	}
						}
					}
				}
			}						
	    
	        $(document).ready(function() {
	            $('#bgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });

	            $('#fgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });
			
	        });
	    </script>        
    </body>
</html>
