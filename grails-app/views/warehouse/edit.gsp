
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
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
										<label for="name"><warehouse:message code="default.name.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'name', 'errors')}">
										<g:textField name="name" value="${warehouseInstance?.name}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.locationType.label" /></label>
		
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}" 
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${warehouseInstance?.locationType?.id}" noSelection="['':'']" />
	                                		
	                                	${warehouseInstance?.locationType?.supportedActivities }
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                	<label for="manager"><warehouse:message code="warehouse.manager.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'manager', 'errors')}">
	                                	<g:select name="manager.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${warehouseInstance?.manager?.id}"  noSelection="['':'']" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
		                                <label for="manager"><warehouse:message code="warehouse.properties.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'local', 'errors')} ${hasErrors(bean: warehouseInstance, field: 'active', 'errors')}">
										<g:checkBox name="local" value="${warehouseInstance?.local}" />
										<warehouse:message code="warehouse.local.label" />

										<g:checkBox name="active" value="${warehouseInstance?.active}" />
										<warehouse:message code="warehouse.active.label" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.locationGroup.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:select name="locationGroup.id" from="${org.pih.warehouse.core.LocationGroup.list()}" optionKey="id" value="${warehouseInstance?.locationGroup?.id}" noSelection="['':'']" />
	                                </td>
	                            </tr>	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
	                                	<g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size()+1 }" style="width: 150px" 
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${warehouseInstance?.supportedActivities}"
	                                		noSelection="['':'']" />
	                                </td>
	                            </tr>	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="bgColor"><warehouse:message code="warehouse.bgColor.label"/></label>
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
	                                  <label for="fgColor"><warehouse:message code="warehouse.fgColor.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'fgColor', 'errors')}">
	                                    <g:select name="fgColor" class="colorpicker" 
	                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
	                                    	value="${warehouseInstance?.fgColor}" />
	                                    	
                                    	<span class="fade">${warehouseInstance?.fgColor }</span>
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
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'active', 'errors')}">
	                                </td>
	                            </tr>
	                            <tr>
	                            	<td valign="top"></td>
	                            	<td valign="top">
					                   <button type="submit">								
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;<warehouse:message code="default.button.save.label"/>
										</button>
										&nbsp;
										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}						
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
