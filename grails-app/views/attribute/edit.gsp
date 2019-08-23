<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${g.message(code: 'attribute.label', default: 'Attribute')}" />
        <title><g:message code="default.${actionName}.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.${createEdit}.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="dialog">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${attributeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${attributeInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="buttonBar">
				<g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
				<g:isUserAdmin>
					<g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
				</g:isUserAdmin>
			</div>

            <g:form method="post">
				<g:hiddenField name="id" value="${attributeInstance?.id}" />
				<g:hiddenField name="version" value="${attributeInstance?.version}" />
				<div class="box">
					<h2><g:message code="default.${actionName}.label" args="[entityName]" /> <small>${attributeInstance.name}</small></h2>

					<table>
						<tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><g:message code="default.active.label" default="Active" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'active', 'errors')}">
                                    <g:checkBox name="active" value="${attributeInstance?.active}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="default.code.label" default="Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'code', 'errors')}">
                                    <g:textField name="code" value="${attributeInstance?.code}" class="text" size="100"
                                                    placeholder="${g.message(code:'attribute.code.placeholder')}"/>
                                </td>
                            </tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="name"><g:message code="default.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'name', 'errors')}">
									<g:textField name="name" value="${attributeInstance?.name}" class="text" size="100"
                                                 placeholder="${g.message(code:'attribute.name.placeholder')}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="description"><g:message code="default.description.label" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'description', 'errors')}">
									<g:textArea name="description" value="${attributeInstance?.description}" class="text" />
								</td>
							</tr>



							<tr class="prop">
								<td valign="top" class="name">
								  <label for="option"><g:message code="attribute.options.label" default="Options" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'options', 'errors')}">
									<table id="optionsTable">
										<tr id="optionRowTemplate" style="display:none;">
											<td>
												<g:textField name="option" value="${option}" size="60" class="text medium"/>
												<a href="#" class="button">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png') }" style="vertical-align: middle;"/>
                                                    <g:message code="default.button.delete.label"/>
												</a>
											</td>
										</tr>
									</table>
                                    <a href="#" onClick="javascript:addOption('');" class="button">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png') }" style="vertical-align: middle;"/>
                                        <warehouse:message code="default.add.option" />
                                    </a>
								</td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="required"><g:message code="default.required.label" default="Required" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: attributeInstance, field: 'required', 'errors')}">
                                    <g:checkBox name="required" value="${attributeInstance?.required}" />
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
						</tbody>
						<tfoot>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons left">
										<g:actionSubmit class="button" action="save" value="${g.message(code: 'default.button.save.label', default: 'Save')}" />
                                        <g:if test="${attributeInstance?.id}">
                                            <g:actionSubmit class="button" action="delete" value="${g.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                        </g:if>
										<g:link action="list" class="button"><g:message code="default.button.cancel.label"/></g:link>
									</div>
								</td>
							</tr>

						</tfoot>
					</table>
				</div>
            </g:form>
        </div>

    <script type="text/javascript">

        var nextIndex = 0;

        $(document).ready(function() {
            <g:if test="${attributeInstance?.options}">
                <g:each var="option" in="${attributeInstance?.options}" status="status">
                    addOption('${option}');
                </g:each>
            </g:if>
            <g:else>
                addOption('');
            </g:else>

            $("input[name='option']").bind('paste', function(event) {
                // Prevent full text from being pasted into field
                event.preventDefault();

                var element = $(this);
                var pasteData = event.originalEvent.clipboardData.getData('text');


                var lines = pasteData.split(/\r\n|\r|\n/);
                console.log("lines: ", lines);
                console.log("lines: ", lines.length);
                $.each(lines, function(index, line){
                    addOption(line);
                });

                // Remove all blank options
                removeBlanks();
            });

        });

        function removeBlanks() {
            console.log("remove blanks");
            var options = $("input[name='option']");
            $.each(options, function(index) {
                var option = $(this);
                if (option.val() == '') {
                    var parent = option.parents().eq(1);
                    console.log(parent);
                    if (parent.is(":not(#optionRowTemplate)")) {
                        parent.remove()
                    }
                }
            });

        }

        function addOption(optionValue) {
            console.log("add option" + optionValue);

            // Clone the template
            var row = $("#optionRowTemplate").clone(true).show();

            // Set ID and class
            $(row).attr("id", "optionRow"+nextIndex).addClass("optionRow").addClass(nextIndex % 2 == 0 ? 'odd' : 'even');

            // Set value
            var input = $(row).find("input[name='option']").val(optionValue);

            // Attach delete action to anchor tag
            $(row).find("a").click(function(event) {
                $(this).parent().remove();
            });

            // Add to table
            $('#optionsTable').append(row);

            // Apply focus to input field after the row has been rendered
            input.focus();
            nextIndex++;
        }
    </script>

    </body>
</html>
