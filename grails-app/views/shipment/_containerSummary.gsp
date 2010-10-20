	<style>
		.selected { font-weight: bold; } 
	</style>
	<g:set var="cssClass">
		<g:if test="${selected?.id == containerInstance?.id}">selected</g:if>
	</g:set>
	
	<div class="${cssClass}">
		<g:if test="${containerInstance?.containerType?.name=='Pallet'}">
			<img src="${createLinkTo(dir:'images/icons',file:'pallet.jpg')}"
				alt="pallet"
				style="vertical-align: middle; width: 24px; height: 24px;" />
		</g:if>
		<g:elseif test="${containerInstance?.containerType?.name=='Suitcase'}">
			<img src="${createLinkTo(dir:'images/icons',file:'suitcase.jpg')}"
				alt="suitcase"
				style="vertical-align: middle; width: 24px; height: 24px;" />
		</g:elseif>
		<g:elseif test="${containerInstance?.containerType?.name=='Container'}">
			<img src="${createLinkTo(dir:'images/icons',file:'container.jpg')}"
				alt="container" style="vertical-align: middle; width: 24px; height: 24px;" />
		</g:elseif>								
		<g:else>														
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}"
				alt="box" style="vertical-align: middle;" />														
		</g:else>
		&nbsp;
		<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':containerInstance?.id]">
			${containerInstance?.containerType?.name} ${containerInstance?.name}		
		</g:link>
	</div>

