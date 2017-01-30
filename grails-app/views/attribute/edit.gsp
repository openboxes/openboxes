<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'attribute.label', default: 'Attribute')}" />
        <g:set var="createEdit" value="${attributeInstance?.id ? 'edit' : 'create'}"/>
        <title><warehouse:message code="default.${createEdit}.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.${createEdit}.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">      
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${attributeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${attributeInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="nav" role="navigation">
				<ul>
					<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
					<li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
					<li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
				</ul>
			</div>

            <g:form method="post" >
                	<g:hiddenField name="id" value="${attributeInstance?.id}" />
                	<g:hiddenField name="version" value="${attributeInstance?.version}" />
	                <div class="dialog box">
						<h2><warehouse:message code="default.${createEdit}.label" args="[entityName]" /></h2>

						<table>
	                        <tbody>


	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><warehouse:message code="default.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${attributeInstance?.name}" class="text" size="100" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="allowOther"><warehouse:message code="attribute.allowOther.label" default="Allow Other" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'allowOther', 'errors')}">
	                                    <g:checkBox name="allowOther" value="${attributeInstance?.allowOther}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="option"><warehouse:message code="attribute.options.label" default="Options" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'options', 'errors')}">
                                		<table id="optionsTable">
		                                    <tr id="optionRowTemplate" style="display:none;">
		                                    	<td>
				                                    <g:textField name="option" value="${option}" size="60" class="text medium"/>
		                                    		<a href="#">
		                                    			<img src="${resource(dir: 'images/icons/silk', file: 'cross.png') }" style="vertical-align: middle;"/>
		                                    		</a>
		                                    	</td>
		                                    </tr>
	                                    </table>
	                                    <div>
	                                    	<a href="#" onClick="javascript:addOption('');">
		                                    	<img src="${resource(dir: 'images/icons/silk', file: 'add.png') }" style="vertical-align: middle;"/>
		                                    	<warehouse:message code="default.add.option" />
		                                   	</a>
	                                    </div>
	                                    
	                                    <script type="text/javascript">
	                                    
											var nextIndex = 0;

											$(document).ready(function() {
			                                    <g:each var="option" in="${attributeInstance?.options}" status="status">
			                                    	addOption('${option}');
			                                    </g:each>
											});
											
											function addOption(optionValue) {
												var row = $("#optionRowTemplate").clone(true).show();
												$(row).attr("id", "optionRow"+nextIndex).addClass(nextIndex % 2 == 0 ? 'odd' : 'even');
												$(row).find("input[name='option']").val(optionValue);
												$(row).find("a").click(function(event) {
													$(this).parent().remove();
												});
												$('#optionsTable').append(row);
												nextIndex++;												
											}
										</script>
	                                </td>
	                            </tr>
	                        	                        
                            	<tr class="prop">
		                        	<td valign="top" class="name"></td>
		                        	<td valign="top" class="value">            	
						                <div class="buttons left">
						                    <g:actionSubmit class="button" action="save" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
						                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
							                <g:link action="list" class="button"><warehouse:message code="default.button.cancel.label"/></g:link>
										</div>
		    						</td>
	                        	</tr>	                        
	                        </tbody>
	                    </table>
	                </div>
            </g:form>
        </div>
    </body>
</html>
