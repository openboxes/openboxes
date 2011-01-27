
<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'attribute.label', default: 'Attribute')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
        
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${attributeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${attributeInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${attributeInstance?.id}" />
	                <g:hiddenField name="version" value="${attributeInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><g:message code="attribute.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${attributeInstance?.name}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="allowOther"><g:message code="attribute.allowOther.label" default="Allow Other" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'allowOther', 'errors')}">
	                                    <g:checkBox name="allowOther" value="${attributeInstance?.allowOther}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="options"><g:message code="attribute.options.label" default="Options" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'options', 'errors')}">
	                                
	                                
										<script type="text/javascript">
											var lastIndex = '${attributeInstance?.options?.size() }';
											function addTableRow() {
												var indexField = $('<td>' + lastIndex + '</td>');
												var optionField = $('<input>')
													.attr('type', 'text').attr('size','40').attr('name','options[' + lastIndex + ']');
												//var removeField = $('<a href="#" onClick="javascript:removeTableRow(' + lastIndex + ')">remove</a>');
												var removeField = '';												
												$('#optionsTable').append(
														$('<tr id="optionRow' + lastIndex + '" class="optionRow">')
															.append(indexField)
															.append($('<td>').append(optionField).append(removeField)
														)
													);											
												//reindexTable()
												lastIndex++;												
											}
											
											function reindexTable() { 
												$('#optionsTable tr').each(function() { 
														//alert("test " + $(this).attr('id')); 
													});												
											}											
											
											function removeTableRow(index) { 
												$('#optionRow' + index).remove();
											}
										
										</script>	                                
	                                
	                                
	                                    <div style="text-align: left;" >
		                                    <a href="#" onClick="javascript: addTableRow();">
		                                    	<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png') }" style="vertical-align: middle;"/>
		                                    	Add Option</a>
	                                    </div>
	                                
                                		<table id="optionsTable">
		                                    <g:each var="option" in="${attributeInstance?.options }" status="status">
			                                    <tr id="optionRow${status }" class="${(status % 2 == 0)?'odd':'even' }">
			                                    	<td>${status }</td>
			                                    	<td>
					                                    <g:textField name="options[${status }]" value="${option }" size="40"/>
			                                    		
			                                    		<g:link class="delete" action="deleteOption" id="${attributeInstance?.id }" params="[selectedOption: option ]">
				                                    		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png') }" style="vertical-align: middle;"/>			                                    			
			                                    		</g:link>
			                                    	</td>
			                                    </tr>
		                                    </g:each>
		                                    <%-- 
		                                    <tr>
		                                    	<td>${attributeInstance?.options?.size() }</td>
		                                    	<td>
				                                    <g:textField name="options[${attributeInstance?.options?.size() }]" value="" size="40"/>
				                                </td>
				                                <td>
				                                    <g:actionSubmit class="save" action="addOption" value="${message(code: 'default.button.add.label', default: 'Add')}" />
				                                </td>
			                                </tr>
			                                --%>
	                                    </table>
	                                </td>
	                            </tr>
	                        	                        
                            	<tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">                        	
						                <div class="buttons">
						                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
						                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
						                </div>
						                
						                <g:link action="list">&lsaquo; back to attributes</g:link>
		    						</td>                    	
	                        	</tr>	                        
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
