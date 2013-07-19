<?xml version="1.0" encoding="UTF-8"?>
<g:if test="${params.includeEntities }">
<!DOCTYPE html [
	<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES Latin 1 for XHTML//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent">
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES Special for XHTML//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent">
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES Symbols for XHTML//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent">
	%HTMLlat1;
	%HTMLspecial;
	%HTMLsymbol;
]>
</g:if>
<html>
<head>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css" media="print, screen, projection"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'wizard.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" media="print, screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="print, screen, projection" />
	<g:javascript library="jquery" plugin="jquery" />
	<g:layoutHead />
	<g:render template="/common/customCss"/>
	<style>
		@page { margin: .25in; }
		@page land { size:landscape; }
		@page port { size:portrait; }
		.landscape { page:land; width: 26.7cm;  }
		.portrait { page:port; }
		.report { -fs-table-paginate: paginate;  }
		.report .total { border-right: 1px solid black; }
		.report th { border-bottom: 1px solid black; }
		<g:if test='${params.insertPageBreakBetweenCategories}'>
			.report { page-break-after: always; }
		</g:if>
	</style>

</head>
<body >
    <div class="${params.orientation?:'landscape'}" >
		<g:layoutBody />
	</div>
</body>
</html>
