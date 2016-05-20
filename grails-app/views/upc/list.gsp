<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'upc.label', default: 'UPC')}" />
        <g:set var="pageTitle" value="${warehouse.message(code: 'default.search.label', args="[entityName]")}" />
        <title><warehouse:message code="default.search.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.search.label" args="[entityName]" /></content>
    </head>
    <body>
		<table width="100%">
			<tr>
				<td valign="top" width="50%">
				</td>
				<td valign="top">
				</td>
			</tr>
		</table>
    </body>
</html>
