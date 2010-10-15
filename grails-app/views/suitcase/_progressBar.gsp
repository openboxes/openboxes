<div style="text-align: center; margin: 10px;">
	<span id="progressbar">
		<span class="step ${(suitaseCommand?.stepNumber==1)?'selected':''}">
			<g:message code="shipment.wizard.step1.label" default="Step 1" /></span> 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
		<span class="step ${(suitaseCommand?.stepNumber==2)?'selected':''}">
			<g:message code="shipment.wizard.step2.label" default="Step 2" /></span> 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
		<span class="step ${(suitaseCommand?.stepNumber==3)?'selected':''}">
			<g:message code="shipment.wizard.step3.label" default="Step 3" /></span> 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
		<span class="step ${(suitaseCommand?.stepNumber==4)?'selected':''}">
			<g:message code="shipment.wizard.step4.label" default="Step 4" /></span> 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
		<span class="step ${(suitaseCommand?.stepNumber==5)?'selected':''}">
			<g:message code="shipment.wizard.step5.label" default="Step 5" /></span> 
	</span>	    
</div>	         