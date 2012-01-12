<html>
<head>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css" media="print, screen, projection"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="print, screen, projection" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'wizard.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tableScroll/',file:'jquery.tablescroll.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" media="print, screen, projection" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="print, screen, projection" />
	<g:layoutHead />
	<g:render template="/common/customCss"/>
	<style>
		@page land { size:landscape; }
		@page port { size:portrait; }
		.landscape { page:land; }
		.portrait { page:port; }
	</style>
</head>
<body style="width: 100%;">
    <div class="${params.orientation?:'landscape'}" style="width: 100%;">
		<g:layoutBody />
	</div>	
</body>
</html>
