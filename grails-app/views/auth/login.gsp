<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Warehouse &gt; Login</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Login</content>
	<content tag="menuTitle">Login</content>		
	<content tag="globalLinksMode">append</content>
	<content tag="localLinksMode">override</content>
	<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
	<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
	<content tag="breadcrumb"><g:render template="breadcrumb" model="[pageTitle:pageTitle]"/></content>
</head>
<body>
	<div class="body">
		<div width="50%">
			<g:render template="../common/login"/>
		</div>			
	</div>
</body>
</html>
