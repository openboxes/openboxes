<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
</head>

<body>
	<div id="root"></div>
	<script src="${createLinkTo(dir:'/js', file:'bundle.js')}" type="text/javascript" ></script>
	<g:if test="${!params.id}">
        You can access the Partial Receiving feature through the details page for an inbound shipment.
	</g:if>
</body>
</html>
