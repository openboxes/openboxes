<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="custom">
		<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="\${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="create-${domainClass.propertyName}" class="content scaffold-create box" role="main">
			<h2><g:message code="default.create.label" args="[entityName]" /></h2>
			<g:if test="\${flash.message}">
			<div class="message" role="status">\${flash.message}</div>
			</g:if>
			<g:hasErrors bean="\${${propertyName}}">
			<ul class="errors" role="alert">
				<g:eachError bean="\${${propertyName}}" var="error">
				<li <g:if test="\${error in org.springframework.validation.FieldError}">data-field-id="\${error.field}"</g:if>><g:message error="\${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form url="[resource:${propertyName}, action:'save']" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
				<div class="form">
					<g:render template="form"/>
                </div>
                <div class="buttons">
                    <g:submitButton name="create" class="save button" value="\${message(code: 'default.button.create.label', default: 'Create')}" />
                </div>
			</g:form>
		</div>
	</body>
</html>
