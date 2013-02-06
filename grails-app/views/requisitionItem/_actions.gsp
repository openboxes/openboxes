<div class="action-menu">
	<button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="pick">
				<img src="${resource(dir: 'images/icons/silk', file: 'box.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.pick.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="substitute">
				<img src="${resource(dir: 'images/icons/silk', file: 'box.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.substitute.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="cancel">
				<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.cancel.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
	</div>
</div>	

