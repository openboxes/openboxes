<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>

<g:if test="${message}">
	<div class="message">${message}</div>
</g:if>
<g:hasErrors bean="${transaction}">
	<div class="errors">
		<g:renderErrors bean="${transaction}" as="list" />
	</div>
</g:hasErrors>
<g:hasErrors bean="${command}">
	<div class="errors">
		<g:renderErrors bean="${command}" as="list" />
	</div>
</g:hasErrors>
<g:hasErrors bean="${flash.itemInstance}">
	<div class="errors dialog">
		<g:renderErrors bean="${flash.itemInstance}" as="list" />
	</div>
</g:hasErrors>
<style>
	.tabs .-primary {
		display: flex;
	}
	.tabs .-primary > li {
		flex-grow: 1;
	}

	.tabs {
		position: relative;
	}
	.tabs .-secondary {
		display: none;
		position: absolute;
		top: 100%;
		right: 0;
		z-index: 1;
	}
	.tabs.--show-secondary .-secondary {
		display: flex;
	}

	.tabs .--hidden {
		display: none;
	}

	.-more {
		display: flex;
		align-items: center;
		vertical-align: middle;
	}

	.more-button {
		background: none;
		border: none;
		color: #c0c0c0;
		padding: 5px;
		outline:none;
		cursor: pointer
	}
</style>

<g:if test="${commandInstance?.inventoryLevel?.status == InventoryStatus.SUPPORTED }">
    <div id="transactionLogTabs" class="tabs">
		<ul class="-primary">
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStock/${commandInstance?.product?.id}"
                   id="current-stock-tab"><warehouse:message code="inventory.listInStock.label" default="In stock"/></a>
            </li>
			<li><a href="${request.contextPath}/inventoryItem/showStockHistory/${commandInstance?.product?.id}"><warehouse:message code="inventory.stockHistory.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStockAllLocations/${commandInstance?.product?.id}"><warehouse:message code="inventory.currentStockAllLocations.label" default="All Locations"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showSuppliers/${commandInstance?.product?.id}"><warehouse:message code="product.sources.label" default="Sources"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showAlternativeProducts/${commandInstance?.product?.id}"><warehouse:message code="product.substitutions.label" default="Substitution"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showPendingInbound/${commandInstance?.product?.id}"><warehouse:message code="stockCard.pendingInbound.label" default="Pending Inbound"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showPendingOutbound/${commandInstance?.product?.id}"><warehouse:message code="stockCard.pendingOutbound.label" default="Pending Outbound"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showDemand/${commandInstance?.product?.id}"><warehouse:message code="forecasting.demand.label" default="Demand"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showInventorySnapshot/${commandInstance?.product?.id}"><warehouse:message code="inventory.snapshot.label" default="Snapshot"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showDocuments/${commandInstance?.product?.id}"><warehouse:message code="document.documents.label" default="Documents"/></a></li>
		</ul>
	</div>
</g:if>
<g:elseif test="${commandInstance?.inventoryLevel?.status == InventoryStatus.NOT_SUPPORTED }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.NOT_SUPPORTED"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.product?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>
<g:elseif test="${commandInstance?.inventoryLevel?.status == InventoryStatus.SUPPORTED_NON_INVENTORY }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.product?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>
<script>
	$(window).load(function(){
		const container = document.querySelector('.tabs')
		const primary = container.querySelector('.-primary')
		const primaryItems = container.querySelectorAll('.-primary > li:not(.-more)')
		let secondary, secondaryItems, allItems, moreLi, moreBtn

		// insert "more" button and duplicate the list
		primary.insertAdjacentHTML('beforeend', `
		  <li class="ui-state-default ui-corner-top -more">
			<button type="button" aria-haspopup="true" aria-expanded="false" class="more-button">
			  More <span>&darr;</span>
			</button>
			<ul class="-secondary">
			</ul>
		  </li>
		`)

		const adjustTab = () => {
			$('.-secondary').empty()
			$('.-primary').children().clone().appendTo('.-secondary')
			$('.-secondary').tabs()
			secondary = container.querySelector('.-secondary')
			secondaryItems = secondary.querySelectorAll('li')
			allItems = container.querySelectorAll('li')
			moreLi = primary.querySelector('.-more')
			moreBtn = moreLi.querySelector('button')
			moreBtn.addEventListener('click', (e) => {
				e.preventDefault()
				moreBtn.setAttribute('aria-expanded', true)
				container.classList.add('--show-secondary')
			})
		}

		// adapt tabs to current width
		const doAdapt = () => {
			// reveal all items for the calculation
			allItems.forEach((item) => {
				item.classList.remove('--hidden')
			})

			// hide items that won't fit in the primary tabs
			let stopWidth = moreBtn.offsetWidth
			let hiddenItems = []
			const primaryWidth = primary.offsetWidth
			primaryItems.forEach((item, i) => {
				if(primaryWidth >= stopWidth + item.offsetWidth) {
					stopWidth += item.offsetWidth
				} else {
					item.classList.add('--hidden')
					hiddenItems.push(i)
				}
			})

			// toggle the visibility of More button and items in Secondary
			if(!hiddenItems.length) {
				moreLi.classList.add('--hidden')
				container.classList.remove('--show-secondary')
				moreBtn.setAttribute('aria-expanded', false)
			}
			else {
				secondaryItems.forEach((item, i) => {
					if(!hiddenItems.includes(i)) {
						item.classList.add('--hidden')
					}
				})
			}
		}

		adjustTab()  // adjust immediately on load
		doAdapt() // adapt immediately on load

		window.addEventListener('resize', doAdapt) // adapt on window resize

		$('li').on('click', function() {
			adjustTab()
			doAdapt()
		})

		$('.-secondary').on("click", "li", function (event) {
			event.preventDefault();
			$( ".tabs" ).tabs({ selected: $(this).index() });
		});

		// hide dropdown with items on the outside click
		document.addEventListener('click', (e) => {
			let el = e.target
			while(el) {
				if(el === secondary || el === moreBtn) {
					return;
				}
				el = el.parentNode
			}
			container.classList.remove('--show-secondary')
			moreBtn.setAttribute('aria-expanded', false)
		})

		// hide dropdown with items on new tab select
		$(".tabs").tabs({
			select: function() {
				container.classList.remove('--show-secondary')
				moreBtn.setAttribute('aria-expanded', false)
			}
		});
	});
</script>
