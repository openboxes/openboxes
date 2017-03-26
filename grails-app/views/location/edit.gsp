
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'larehouse.label', default: 'Location')}" />
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
            <g:hasErrors bean="${locationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:render template="summary"/>

            <g:form method="post" action="update">
                <g:hiddenField name="id" value="${locationInstance?.id}" />
                <g:hiddenField name="version" value="${locationInstance?.version}" />
                <div class="dialog">
                    <div>

                    <div id="location-tabs" class="tabs">
                        <ul>
                            <li><a href="#location-details-tab"><warehouse:message code="location.label"/></a></li>
                            <li><a href="#location-status-tab"><warehouse:message code="location.status.label" default="Status"/></a></li>
                            <li><a href="#location-address-tab"><warehouse:message code="location.address.label" default="Address"/></a></li>
                        </ul>
                        <div id="location-details-tab">
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_detail.png')}" class="middle"/>
                                    <warehouse:message code="location.details.label" default="Details"/>
                                </h2>
                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="default.name.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                                                <g:textField name="name" value="${locationInstance?.name}" class="text" size="80"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="location.locationType.label" /></label>

                                            </td>
                                            <td valign="top" class="value">
                                                <g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}" class="chzn-select-deselect"
                                                    optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.locationType?.id}" noSelection="['null':'']" />


                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="location.locationGroup.label" /></label>
                                            </td>
                                            <td valign="top" class="value">
                                                <g:select class="chzn-select-deselect"
                                                        name="locationGroup.id" from="${org.pih.warehouse.core.LocationGroup.list()}"
                                                          optionKey="id" value="${locationInstance?.locationGroup?.id}" noSelection="['null':'']" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="manager.id"><warehouse:message code="warehouse.manager.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
                                                <g:select class="chzn-select-deselect"
                                                          name="manager.id" from="${org.pih.warehouse.core.User.list().sort{it.lastName}}" optionKey="id" value="${locationInstance?.manager?.id}"  noSelection="['null':'']" />
                                            </td>
                                        </tr>


                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="bgColor"><warehouse:message code="location.bgColor.label"/></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
                                                <g:textField name="bgColor" value="${locationInstance?.bgColor}" class="text" size="10"/>
                                                <%--
                                                <g:select name="bgColor" class="colorpicker"
                                                    from="${org.pih.warehouse.core.Constants.COLORS}"
                                                    value="${locationInstance?.bgColor}" />

                                                <span class="fade">#${locationInstance?.bgColor }</span>
                                                 --%>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="fgColor"><warehouse:message code="location.fgColor.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
                                                <g:textField name="fgColor" value="${locationInstance?.fgColor}" class="text" size="10"/>
                                                <%--
                                                <g:select name="fgColor" class="colorpicker"
                                                    from="${org.pih.warehouse.core.Constants.COLORS}"
                                                    value="${locationInstance?.fgColor}"/>

                                                <span class="fade">#${locationInstance?.fgColor }</span>
                                                --%>
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="bgColor"><warehouse:message code="location.sortOrder.label" default="Sort order"/></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'sortOrder', 'errors')}">
                                                <g:textField name="sortOrder" value="${locationInstance?.sortOrder}" class="text" size="10"/>
                                                <%--
                                                <g:select name="bgColor" class="colorpicker"
                                                    from="${org.pih.warehouse.core.Constants.COLORS}"
                                                    value="${locationInstance?.bgColor}" />

                                                   <span class="fade">#${locationInstance?.bgColor }</span>
                                                    --%>
                                            </td>
                                        </tr>

                                        <%--
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="parentLocation"><warehouse:message code="warehouse.parentLocation.label" default="Parent Location" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">
                                                <g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}"
                                                    optionKey="id" optionValue="name" value="" noSelection="['null': '']" />
                                            </td>
                                        </tr>
                                        --%>

                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <button type="submit" class="button icon approve">
                                                    <warehouse:message code="default.button.save.label"/>
                                                </button>
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
                        </div>
                        <div id="location-status-tab">
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'flag_red.png')}" class="middle"/>
                                    <warehouse:message code="default.status.label" default="Status"/>
                                </h2>
                                <table>
                                    <tbody>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="active"><warehouse:message code="warehouse.active.label" /></label>
                                        </td>
                                        <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
                                                <g:checkBox name="active" value="${locationInstance?.active}" />

                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="local"><warehouse:message code="warehouse.local.label" /></label>
                                        </td>
                                        <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
                                            <g:checkBox name="local" value="${locationInstance?.local}" />
                                        </td>
                                    </tr>


                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
                                            <g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size()+1 }" style="width: 150px"
                                                      optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.supportedActivities?:locationInstance?.locationType?.supportedActivities}"
                                                      noSelection="['':warehouse.message(code:'location.useDefaultActivities.label')]" />

                                        </td>
                                    </tr>
                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <button type="submit" class="button icon approve">
                                                    <warehouse:message code="default.button.save.label"/>
                                                </button>
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
                        </div>
                        <div id="location-address-tab">
                            <g:hiddenField name="address.id" value="${locationInstance?.address?.id}"/>
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" class="middle"/>
                                    <warehouse:message code="address.label" default="Address"/>
                                </h2>
                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.address.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'address', 'errors')}">
                                                <g:textField name="address.address" value="${locationInstance?.address?.address}" class="text" size="60"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.address2.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'address2', 'errors')}">
                                                <g:textField name="address.address2" value="${locationInstance?.address?.address2}" class="text" size="60"/>
                                            </td>
                                        </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="address.city.label" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'city', 'errors')}">
                                            <g:textField name="address.city" value="${locationInstance?.address?.city}" class="text" size="60"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="address.stateOrProvince.label" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'stateOrProvince', 'errors')}">
                                            <g:textField name="address.stateOrProvince" value="${locationInstance?.address?.stateOrProvince}" class="text" size="60"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="address.postalCode.label" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'postalCode', 'errors')}">
                                            <g:textField name="address.postalCode" value="${locationInstance?.address?.postalCode}" class="text" size="60"/>
                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="address.country.label" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'country', 'errors')}">
                                            <g:textField name="address.country" value="${locationInstance?.address?.country}" class="text" size="60"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="address.description.label" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'description', 'errors')}">
                                            <g:textArea name="address.description" value="${locationInstance?.address?.description}" class="text" rows="6" cols="80"/>
                                        </td>
                                    </tr>
                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <button type="submit" class="button icon approve">
                                                    <warehouse:message code="default.button.save.label"/>
                                                </button>
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
                        </div>

                    </div>

                </div>
               
            </g:form>
        </div>
	    <script type="text/javascript">

            $(document).ready(function() {

                $(".tabs").tabs({
                    cookie : {
                        expires : 1
                    }
                });
            });

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
	    	/*
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
	        */
	    </script>        
    </body>
</html>
