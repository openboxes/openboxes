
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationGroup.label', default: 'Location Group')}" />
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
            <g:hasErrors bean="${locationGroupInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationGroupInstance}" as="list" />
	            </div>
            </g:hasErrors>
                        
			<g:render template="summary"/>

            <div class="button-bar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[entityName]"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[entityName]"/></g:link>
            </div>


            <g:form method="post">
                <g:hiddenField name="id" value="${locationGroupInstance?.id}" />
                <g:hiddenField name="address.id" value="${locationGroupInstance?.address?.id}" />
                <g:hiddenField name="version" value="${locationGroupInstance?.version}" />
                <div class="dialog box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="name"><warehouse:message code="default.name.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance, field: 'name', 'errors')}">
									<g:textField name="name" value="${locationGroupInstance?.name}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.address.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance?.address, field: 'address', 'errors')}">
                                    <g:textField name="address.address" value="${locationGroupInstance?.address?.address}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.address2.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'address2', 'errors')}">
                                    <g:textField name="address.address2" value="${locationGroupInstance?.address?.address2}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.city.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance?.address, field: 'city', 'errors')}">
                                    <g:textField name="address.city" value="${locationGroupInstance?.address?.city}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.stateOrProvince.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'stateOrProvince', 'errors')}">
                                    <g:textField name="address.stateOrProvince" value="${locationInstance?.address?.stateOrProvince}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.postalCode.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance?.address, field: 'postalCode', 'errors')}">
                                    <g:textField name="address.postalCode" value="${locationGroupInstance?.address?.postalCode}" class="text" size="80"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.country.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance?.address, field: 'country', 'errors')}">
                                    <g:textField name="address.country" value="${locationGroupInstance?.address?.country}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="address.description.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: locationGroupInstance?.address, field: 'description', 'errors')}">
                                    <g:textArea name="address.description" value="${locationGroupInstance?.address?.description}" class="text" rows="6" cols="80"/>
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr class="prop">
                            
                            	<td valign="top">
                            	
                            	</td>
                            	<td valign="top">
									<div class="buttons left">
                                        <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />

                                        <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />

                                        &nbsp;
										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label')}						
										</g:link>			
									</div>
								</td>
							</tr>
                            
                        </tfoot>
                    </table>
                </div>
	                
               
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
	    </script>        
    </body>
</html>
