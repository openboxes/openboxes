

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
		<img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}"
			alt="box" style="vertical-align: middle; width: 24px; height: 24px;" />														
	</g:else>
	<b>${containerInstance?.containerType?.name} ${containerInstance?.name}</b>		

