<%@ page import="org.pih.warehouse.core.ActivityCode" defaultCodec="html" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="${params.print?'print':'custom' }" />
	<title><warehouse:message code="report.showTransactionReport.label" /></title>

	<style>
	.chosen-container-multi .chosen-choices li.search-field input[type="text"] {
		height: 26px;
		line-height: 26px;
	}

	.location-container {
		height: 120px;
	}

	.location-container .chosen-container-multi .chosen-choices {
		height: 120px !important;
		overflow-y: auto;
	}

	/* The report period field and its range picker */
	.report-period input[type=text] {
		width: 100%;
		box-sizing: border-box;
	}

	.report-period .hint {
		cursor: help;
	}

	/* The picker sizes itself assuming content-box, so its months wrap onto two lines with the site's border-box */
	.date-picker-wrapper, .date-picker-wrapper * {
		box-sizing: content-box;
	}

	/* The picker's shortcuts, one line per group (days, months, quarters, years) */
	.date-picker-wrapper .footer .shortcuts {
		line-height: 1.8;
	}

	.date-picker-wrapper .shortcut-group {
		display: block;
	}

	.date-picker-wrapper .shortcut-group-label {
		display: inline-block;
		width: 70px;
		color: #666;
	}

	.date-picker-wrapper .shortcut-group .custom-shortcut {
		margin-right: 10px;
	}

	/* The message in the empty table, before a report has been run or when it has no rows */
	#transactionReport_wrapper td.dataTables_empty {
		padding: 40px 20px;
		text-align: center;
		color: #666;
		font-size: 1.1em;
	}

	/* Run and download buttons side by side, filling the width of the sidebar */
	.sidebar .buttons {
		display: flex;
		gap: 10px;
		padding: 10px;
	}

	.sidebar .buttons .button {
		flex: 1;
		justify-content: center;
		margin: 0;
	}

	.required-marker {
		color: #c00;
	}

	/* The optional filters, set apart from the required parameters above them */
	.product-filters {
		margin-top: 15px;
		border-top: 1px solid #ddd;
		padding-top: 5px;
	}

	.product-filters h3 {
		margin: 10px 10px 0;
		font-size: 1em;
		font-weight: bold;
	}

	.product-filters-hint {
		margin: 3px 10px 0;
		color: #666;
		font-size: 0.9em;
	}

	/* Covers the whole page (including the header and footer) while the report is being generated, so that it can't
	   be run again until it's done (see #6201) */
	.report-overlay {
		display: none;
		position: fixed;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		z-index: 10000;
		align-items: center;
		justify-content: center;
		background-color: rgba(128, 128, 128, 0.5);
		cursor: wait;
	}

	.report-overlay.active {
		display: flex;
	}

	.report-overlay-message {
		width: 320px;
		padding: 20px;
		background-color: #fff;
		border-radius: 4px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
		text-align: center;
		color: #333;  /* Set explicitly so that the site's alert styles don't change the text color */
	}

	.report-overlay-title {
		font-size: 1.2em;
		font-weight: bold;
	}

	.report-overlay-title img {
		vertical-align: middle;
		margin-right: 5px;
	}

	.report-overlay-elapsed {
		margin: 10px 0;
		font-size: 1.1em;
	}

	.report-overlay-hint {
		color: #666;
	}

	/* Transaction report table. Scoped to the wrapper because DataTables moves the header and footer into their
	   own tables when the table scrolls. */
	#transactionReport_wrapper th,
	#dialogDataTable_wrapper th {
		font-weight: 600;
	}

	/* Same horizontal padding in the header, body and footer so that the columns line up */
	#transactionReport_wrapper th,
	#dialogDataTable_wrapper th,
	#transactionReport_wrapper td,
	#dialogDataTable_wrapper td {
		padding: 6px 10px;
	}

	/* Keep the sort icon on the same line as the column name */
	#transactionReport_wrapper .DataTables_sort_wrapper,
	#dialogDataTable_wrapper .DataTables_sort_wrapper {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	#transactionReport_wrapper th.right .DataTables_sort_wrapper,
	#dialogDataTable_wrapper th.right .DataTables_sort_wrapper {
		justify-content: flex-end;
	}

	#transactionReport_wrapper .DataTables_sort_icon,
	#dialogDataTable_wrapper .DataTables_sort_icon {
		flex: none;
	}

	#transactionReport_wrapper th.right,
	#dialogDataTable_wrapper th.right,
	#transactionReport_wrapper td.right,
	#dialogDataTable_wrapper td.right {
		text-align: right;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	/* Transaction dates in the details dialog: fixed-width digits so that they line up, a lighter time, a dimmed date
	   when it's the same as the row above, and a line above the first row of each day */
	#dialogDataTable_wrapper td.date-cell {
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	#dialogDataTable_wrapper td.date-cell .time {
		color: #999;
	}

	#dialogDataTable_wrapper td.date-cell .date.same-date {
		color: #ccc;
	}

	#dialogDataTable_wrapper tbody tr.new-day td {
		border-top: 1px solid #ddd;
	}

	/* "Ghost" rows in the details dialog: changes that no transaction recorded */
	#dialogDataTable_wrapper tbody tr.ghost-row td {
		font-style: italic;
		color: #888;
		cursor: help;
	}

	#dialogDataTable_wrapper tbody tr.ghost-row td:nth-child(2) {
		border-left: 3px dashed #ccc;
	}

	/* Internal transfers in the details dialog: they move stock between bins without changing the balance */
	#dialogDataTable_wrapper tbody tr.internal-row td,
	#dialogDataTable_wrapper tbody tr.internal-row td.credit,
	#dialogDataTable_wrapper tbody tr.internal-row td.debit,
	#dialogDataTable_wrapper tbody tr.internal-row td a {
		color: #aaa;
	}

	#dialogDataTable_wrapper tbody tr.internal-row {
		cursor: help;
	}

	.internal-transfers-toggle {
		cursor: help;
	}

	/* Details dialog: the summary of the product's period, like its row in the report */
	.dialog-summary {
		padding: 4px 0 8px;
	}

	/* The internal transfers option, on the right of the tab bar. No z-index: the modal dialog ignores clicks on
	   elements with a z-index below its overlay's (jQuery UI's .zIndex() stops at the first one it finds). */
	#dialogTabs {
		position: relative;
	}

	#dialogTabs .internal-transfers-toggle {
		position: absolute;
		top: 12px;
		right: 10px;
	}

	/* A row of figures, each a small label above a quantity */
	.dialog-summary-equation {
		display: flex;
		align-items: flex-end;
		gap: 4px;
		font-variant-numeric: tabular-nums;
	}

	.dialog-stat {
		display: inline-flex;
		flex-direction: column;
		padding: 2px 8px;
		border-radius: 3px;
		color: inherit;
		text-decoration: none;
	}

	.dialog-stat-label {
		font-size: 0.8em;
		text-transform: uppercase;
		letter-spacing: 0.03em;
		color: #777;
	}

	.dialog-stat-value {
		font-size: 1.25em;
	}

	/* The opening and closing balances, set apart from the activity between them */
	.dialog-stat-balance {
		background-color: #f1f3f5;
	}

	.dialog-stat-balance .dialog-stat-value {
		font-weight: 600;
	}

	.dialog-summary .credit {
		color: #2e7d32;
	}

	.dialog-summary .debit {
		color: #c62828;
	}

	/* The figures carry their own sign, so leave out the parentheses that .debit adds (openboxes.css) */
	.dialog-summary .debit::before,
	.dialog-summary .debit::after {
		content: none;
	}

	.dialog-summary .zero {
		color: #bbb;
	}

	a.dialog-summary-figure:hover .dialog-stat-value {
		text-decoration: underline;
	}

	a.dialog-summary-figure.active {
		background-color: #e8f0fa;
	}

	a.dialog-summary-figure.active .dialog-stat-label {
		color: #1a5ea8;
		font-weight: 600;
	}

	.dialog-stat-operator {
		align-self: center;
		padding-top: 12px;
		font-size: 1.25em;
		color: #999;
	}

	.internal-transfers-count,
	.dialog-tab-count {
		color: #666;
	}

	/* The filter that's shown, on the left of the table's search row */
	.dialog-filter {
		float: left;
		margin: 2px 0 0 4px;
		padding: 2px 8px;
		background-color: #e8f0fa;
		border-radius: 3px;
		font-weight: normal;
	}

	#dialogTabs .ui-tabs-panel {
		padding: 8px 0 0;
	}

	/* The By type tab */
	table.dialog-by-type {
		width: 100%;
		border-collapse: collapse;
		font-variant-numeric: tabular-nums;
	}

	table.dialog-by-type th,
	table.dialog-by-type td {
		padding: 5px 8px;
		border-bottom: 1px solid #eee;
	}

	table.dialog-by-type th {
		text-align: left;
		background-color: #f0f0f2;
	}

	table.dialog-by-type th.right,
	table.dialog-by-type td.right {
		text-align: right;
	}

	table.dialog-by-type tr.special td {
		font-style: italic;
	}

	table.dialog-by-type tr.not-counted td,
	table.dialog-by-type tr.not-counted td a {
		color: #aaa;
	}

	table.dialog-by-type tfoot td {
		font-weight: 600;
		border-top: 2px solid #ccc;
		border-bottom: none;
	}

	table.dialog-by-type td.credit {
		color: #2e7d32;
	}

	table.dialog-by-type td.debit {
		color: #c62828;
	}

	/* Fixed layout, so that the columns keep their widths and the product name gets the rest. Otherwise the browser
	   spreads the extra width over every column, which pushes the quantities away from the product. Applies to the
	   header and body tables, which DataTables splits the table into when it scrolls. */
	#transactionReport_wrapper table.dataTable {
		table-layout: fixed;
	}

	/* The column widths. DataTables sets its own width on every header cell (and doesn't keep the widths it's given),
	   so they're set here instead. The product name has no width, so it gets the rest. */
	#transactionReport_wrapper table.dataTable thead th:nth-child(1) {
		width: 80px !important;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(2) {
		width: auto !important;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(3) {
		width: 260px !important;
	}

	/* The site limits data table cells to 100px wide */
	#transactionReport_wrapper table.dataTable th:nth-child(2),
	#transactionReport_wrapper table.dataTable td:nth-child(2),
	#transactionReport_wrapper table.dataTable th:nth-child(3),
	#transactionReport_wrapper table.dataTable td:nth-child(3) {
		max-width: none;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(n+4) {
		width: 75px !important;
	}

	/* Show at most two lines of long product and category names (the full name is in the tooltip) */
	#transactionReport_wrapper .clamp {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	/* Zebra stripes, overriding the DataTables colors for the sorted column */
	#transactionReport_wrapper tbody tr.odd td,
	#dialogDataTable_wrapper tbody tr.odd td,
	#transactionReport_wrapper tbody tr.odd td.sorting_1,
	#dialogDataTable_wrapper tbody tr.odd td.sorting_1 {
		background-color: #fff;
	}

	#transactionReport_wrapper tbody tr.even td,
	#dialogDataTable_wrapper tbody tr.even td,
	#transactionReport_wrapper tbody tr.even td.sorting_1,
	#dialogDataTable_wrapper tbody tr.even td.sorting_1 {
		background-color: #f7f7f9;
	}

	/* Rows open the product's transactions when clicked */
	#transactionReport_wrapper tbody tr:hover td,
	#transactionReport_wrapper tbody tr:hover td.sorting_1 {
		background-color: #eaf2fb;
		cursor: pointer;
	}

	/* Except the message in an empty table, which isn't a product */
	#transactionReport_wrapper tbody tr:hover td.dataTables_empty {
		background-color: #fff;
		cursor: default;
	}

	#transactionReport_wrapper .credit,
	#dialogDataTable_wrapper .credit {
		color: #2e7d32;
	}

	#transactionReport_wrapper .debit,
	#dialogDataTable_wrapper .debit {
		color: #c62828;
	}

	#transactionReport_wrapper .zero,
	#dialogDataTable_wrapper .zero {
		color: #bbb;
	}

	/* A column header with a tooltip */
	#transactionReport_wrapper th .hint {
		cursor: help;
		border-bottom: 1px dotted;
	}

	#transactionReport_wrapper a,
	#dialogDataTable_wrapper a {
		color: #1a5ea8;
	}

	/* Details dialog: the opening and closing balances stand out from the transactions */
	#dialogDataTable_wrapper tbody tr.balance-row td {
		font-weight: 600;
		background-color: #f0f0f2;
	}

	/* The period label, with the links to the period before and after on the right */
	.report-period-header {
		display: flex;
		justify-content: space-between;
		align-items: baseline;
	}

	.report-period-steps a {
		margin-left: 8px;
		font-size: 0.9em;
		color: #1a5ea8;
		text-decoration: none;
	}

	.report-period-steps a:hover {
		text-decoration: underline;
	}

	.report-period-steps a.disabled {
		color: #bbb;
		cursor: default;
		text-decoration: none;
	}

	/* The summary counts in the metadata box filter the table */
	.report-metadata-summary a {
		color: #1a5ea8;
	}

	.report-metadata-summary a.active {
		font-weight: 600;
	}

	.report-metadata-summary .zero {
		color: #999;
	}

	.report-metadata-filter-clear {
		margin-left: 6px;
		font-size: 0.9em;
	}

	.report-metadata-empty {
		padding: 10px;
		color: #666;
	}

	/* Counted quantities don't change the balance */
	#dialogDataTable_wrapper .counted {
		color: #999;
		font-style: italic;
		cursor: help;
	}
	</style>
	<%-- The same date range picker as the request list --%>
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.4/moment.min.js"></script>
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/jquery.daterangepicker.min.js"></script>
</head>
<body>

	<div class="body">
		<div class="report-overlay" role="alert" aria-busy="true">
			<div class="report-overlay-message">
				<div class="report-overlay-title">
					<img alt="spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" />
					<g:message code="report.generating.label" default="Generating report..."/>
				</div>
				<div class="report-overlay-elapsed">
					<g:message code="report.elapsed.label" default="Elapsed"/>: <span class="report-overlay-elapsed-time">0:00</span>
				</div>
				<div class="report-overlay-hint">
					<g:message code="report.generating.hint" default="Large date ranges can take a few minutes. Please don't refresh the page or click again."/>
				</div>
			</div>
		</div>
		<g:if test="${flash.message}">
			<div class="message" role="status" aria-label="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors" role="alert" aria-label="error-message">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>


		<div class="yui-gf">

			<div class="yui-u first">

				<div class="sidebar">

					<div class="box">
						<h2><warehouse:message code="report.parameters.label" default="Parameters"/></h2>

						<div class="parameters">

							<div class="filter-list-item">
								<label>
									<warehouse:message code="report.facility.label" default="Facility"/>
									<span class="required-marker">*</span>
								</label>
								<g:selectLocation
									class="chzn-select-deselect filter"
									id="locationId"
									name="location.id"
									activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"
									noSelection="['':'']"
									maxChars="75"
									groupBy="locationType"
									value="${command?.location?.id}"
								/>
							</div>
							<%-- The report period, picked as a range. The hidden fields hold the dates in the format the server reads
							     (MM/dd/yyyy), and are filled in from the text field by syncReportPeriod. --%>
							<div class="filter-list-item report-period">
								<div class="report-period-header">
									<label for="reportPeriod" class="hint" title="${g.message(code: 'report.period.hint', default: 'Both dates are included: from the start of the first day to the end of the last.')}">
										<g:message code="report.period.label" default="Period"/>
										<span class="required-marker">*</span>
									</label>
									<%-- Move to the period before or after, of the same length (e.g. the month before) --%>
									<span class="report-period-steps">
										<a href="#" class="report-period-previous">&lsaquo; <g:message code="report.period.previous.label" default="Previous"/></a>
										<a href="#" class="report-period-next"><g:message code="report.period.next.label" default="Next"/> &rsaquo;</a>
									</span>
								</div>
								<input type="text" id="reportPeriod" class="text filter" autocomplete="off"
									   placeholder="${g.message(code: 'report.period.placeholder', default: 'e.g. 01/Jan/2026 - 31/Jan/2026')}"/>
								<input type="hidden" id="startDate" name="startDate" value="${formatDate(date: startDate, format: 'MM/dd/yyyy')}"/>
								<input type="hidden" id="endDate" name="endDate" value="${formatDate(date: endDate, format: 'MM/dd/yyyy')}"/>
							</div>

							<%-- Optional filters, ordered from broadest to narrowest. The report combines them with AND. --%>
							<div class="product-filters">
								<h3><g:message code="report.productFilters.label" default="Filter products (optional)"/></h3>
								<p class="product-filters-hint">
									<g:message code="report.productFilters.hint" default="Leave blank for all products. Products must match every filter you set."/>
								</p>
								<div class="filter-list-item">
									<label><warehouse:message code="category.label"/></label>
									<p>
										<g:selectCategory
											id="category"
										    class="chzn-select-deselect filter"
										    data-placeholder="${g.message(code: 'category.selectCategory.label', default: 'Select a category')}"
										    name="category"
										    noSelection="['':'']"
										    value="${params?.category ?: command?.rootCategory?.id}"
										/>
									</p>
									<p>
										<label>
											<%-- Checked by default. A report link (run=true) leaves it out when it was unchecked. --%>
											<g:checkBox name="includeCategoryChildren" value="true"
														checked="${params.run == 'true' ? params.includeCategoryChildren != null : true}"/>
											${warehouse.message(
												code:'report.search.includeCategoryChildren.label',
												default: 'Include all products in all subcategories',
											)}
										</label>
									</p>
								</div>
								<div class="filter-list-item">
									<label><warehouse:message code="catalogs.name.label"/></label>
									<p>
										<g:selectCatalogs
											id="catalogs"
											name="catalogs"
											noSelection="['null':'']"
										    value="${params?.catalogs}"
											data-placeholder="${g.message(code: 'default.selectOptions.label', default: 'Select Options')}"
											class="chzn-select-deselect"
										/>
									</p>
								</div>
								<div class="filter-list-item">
									<label><warehouse:message code="tags.label" default="Tags"/></label>
									<p>
										<g:selectTags
											name="tags"
										    id="tags"
										    value="${params?.tags}"
											data-placeholder="${g.message(code: 'default.selectOptions.label', default: 'Select Options')}"
											multiple="true"
										    class="chzn-select-deselect"
										/>
									</p>
								</div>
								<div class="filter-list-item">
									<label><g:message code="products.label" default="Products"/></label>
									<p>
										<g:selectProductAjax id="products-select"
															 value="${products}"
															 multiple="true"
										/>
									</p>
								</div>
							</div>
							<div class="filter-list-item">
								<label class="hint" title="${g.message(code: 'report.transaction.includeProductsWithNoActivity.hint', default: 'Also lists products that had stock but no activity, i.e. their credits, debits and differences are all 0.')}">
									<g:checkBox name="includeProductsWithNoActivity" value="${params?.includeProductsWithNoActivity}"/>
									<g:message code="report.transaction.includeProductsWithNoActivity.label" default="Include products with no activity"/>
								</label>
							</div>
							<div class="filter-list-item">
								<label class="hint" title="${g.message(code: 'report.transaction.includeInternalTransfers.hint', default: 'Counts transfers between bin locations of the facility (e.g. putaways and stock transfers) in the credits and debits. They move stock between bins without changing the facility\'s quantity on hand, so they\'re left out by default.')}">
									<g:checkBox name="includeInternalTransfers" value="${params?.includeInternalTransfers}"/>
									<g:message code="report.transaction.includeInternalTransfers.label" default="Include internal transfers"/>
								</label>
							</div>
						</div>
						<div class="buttons">
							<button class="submit-button button primary">
								<img src="${resource(dir:'images/icons/silk',file:'play_blue.png')}" />
								<g:set var="reportLabel" value="${g.message(code:'default.report.label', default: 'Report')}"/>
								${g.message(code: 'default.run.label', args: [reportLabel])}
							</button>
							<button class="download-button button"
									title="${g.message(code: 'report.transaction.download.hint', default: 'Downloads the report as a CSV file, with a column for each transaction type')}">
								<img src="${resource(dir:'images/icons/silk',file:'page_excel.png')}" />
								<g:set var="csvLabel" value="${g.message(code:'default.csv.label', default: 'CSV')}"/>
								${g.message(code: 'default.download.label', args: [csvLabel])}
							</button>
						</div>
					</div>
					<%-- Metadata of the last report that was run. Filled in by showReportMetadata. --%>
					<div class="box report-metadata">
						<h2><g:message code="report.metadata.label" default="Report Metadata"/></h2>
						<p class="report-metadata-empty">
							<g:message code="report.metadata.empty" default="Run the report to see its details here."/>
						</p>
						<table class="report-metadata-values" style="display: none">
							<tbody>
								<tr class="prop">
									<td class="name"><label><g:message code="report.facility.label" default="Facility"/></label></td>
									<td class="value report-metadata-facility"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.period.label" default="Period"/></label></td>
									<td class="value report-metadata-period"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.filters.label" default="Filters"/></label></td>
									<td class="value report-metadata-filters"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.included.label" default="Included"/></label></td>
									<td class="value report-metadata-included"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="products.label" default="Products"/></label></td>
									<td class="value report-metadata-products"></td>
								</tr>
								<%-- Products to look into. Each one filters the table to those products. --%>
								<tr class="prop">
									<td class="name"><label class="hint" title="${g.message(code: 'report.metadata.withDifferences.hint', default: 'Products whose counts changed their quantity, i.e. Differences is not 0')}"><g:message code="report.metadata.withDifferences.label" default="With differences"/></label></td>
									<td class="value report-metadata-summary" data-summary="differences"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label class="hint" title="${g.message(code: 'report.metadata.stockouts.hint', default: 'Products that had stock at the start of the period and none at the end')}"><g:message code="report.metadata.stockouts.label" default="Stockouts"/></label></td>
									<td class="value report-metadata-summary" data-summary="stockouts"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label class="hint" title="${g.message(code: 'report.metadata.negativeClosing.hint', default: 'Products with a negative closing balance, which usually means a transaction is missing or wrong')}"><g:message code="report.metadata.negativeClosing.label" default="Negative closing"/></label></td>
									<td class="value report-metadata-summary" data-summary="negativeClosing"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.generated.label" default="Generated"/></label></td>
									<td class="value report-metadata-generated"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.duration.label" default="Duration"/></label></td>
									<td class="value report-metadata-duration"></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="yui-u">
				<div class="box">
					<h2>
						${warehouse.message(code:'report.details.label', default: "Report Details")}
					</h2>

                    <table id="transactionReport" class="dataTable">
                        <thead>
                        <tr>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.code.hint', default: "The product code")}"><warehouse:message code="product.productCode.label" default="Code" /></span></th>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.product.hint', default: "The product name. Click a row to see the product's transactions.")}"><warehouse:message code="product.label" default="Product" /></span></th>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.category.hint', default: "The category of the product")}"><warehouse:message code="category.label" default="Category" /></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.openingBalance.hint', default: "Quantity on hand at the beginning of the start date")}"><warehouse:message code="report.transaction.openingBalance.label" default="Opening"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.credits.hint', default: "Quantity added by credit transactions during the period, e.g. receipts, transfers in and adjustments up")}"><warehouse:message code="default.credits.label" default="Credits"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.debits.hint', default: "Quantity removed by debit transactions during the period, e.g. issues, transfers out and adjustments down")}"><warehouse:message code="default.debits.label" default="Debits"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.differences.hint', default: "The change in quantity from counts that didn't match the balance, i.e. the change that credits and debits don't account for")}"><warehouse:message code="report.transaction.differences.label" default="Differences"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.closingBalance.hint', default: "Quantity on hand at the end of the end date (or now, if the end date is today or later)")}"><warehouse:message code="report.transaction.closingBalance.label" default="Closing"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
				</div>
			</div>
		</div>
	</div>
	<div class="loading">
		Loading...
	</div>
<script>

    function handleAjaxError(xhr, status, error) {
        if (status === 'timeout') {
            errorMessage('The server took too long to send the data.');
        } else {
            if (xhr.responseText) {
                var errorMessage = JSON.parse(xhr.responseText).errorMessage;
                alert(errorMessage);
            }
        }
    }

    // Only one report request can be in progress at a time. While it is, the report is covered by an overlay
    // and the buttons are disabled, so that it can't be run again until it's done (see #6201).
    var reportRequestInProgress = false;
    var reportElapsedTimer = null;
    var reportStartTime = null;
    var reportTimeout = ${grailsApplication.config.openboxes.report.transactionReport.timeout ?: 300000};

    function startReportRequest() {
        if (reportRequestInProgress) {
            return false;
        }
        reportRequestInProgress = true;
        $(".submit-button, .download-button").prop("disabled", true);

        var startTime = reportStartTime = Date.now();
        $(".report-overlay-elapsed-time").text("0:00");
        reportElapsedTimer = setInterval(function () {
            var seconds = Math.floor((Date.now() - startTime) / 1000);
            var remainder = seconds % 60;
            $(".report-overlay-elapsed-time").text(Math.floor(seconds / 60) + ":" + (remainder < 10 ? "0" : "") + remainder);
        }, 1000);
        $(".report-overlay").addClass("active");
        return true;
    }

    function finishReportRequest() {
        clearInterval(reportElapsedTimer);
        $(".report-overlay").removeClass("active");
        $(".submit-button, .download-button").prop("disabled", false);
        reportRequestInProgress = false;
    }

    // Returns a message to show the user for a failed report request
    function getReportErrorMessage(status, responseText) {
        if (status === "timeout") {
            return "${g.message(code: 'report.timeout.message', default: 'The report took too long to generate. Try a shorter date range or fewer products.')}";
        }
        var message = "${g.message(code: 'report.error.message', default: 'An unexpected error has occurred on the server. Please contact your system administrator.')}";
        try {
            var errorMessage = JSON.parse(responseText).errorMessage;
            if (errorMessage) {
                return message + "\n\n" + errorMessage;
            }
        } catch (e) {
            // The response wasn't JSON (e.g. an error page), so fall back to the generic message
        }
        return message;
    }

    // Downloads the report in the background (instead of navigating to it) so that we know when it's done and can
    // show any error, and so that clicking again doesn't silently cancel the first download.
    function downloadReport(url) {
        if (!startReportRequest()) {
            return;
        }
        var xhr = new XMLHttpRequest();
        xhr.open("GET", url);
        xhr.responseType = "blob";
        xhr.timeout = reportTimeout;
        xhr.onload = function () {
            var contentType = xhr.getResponseHeader("Content-Type") || "";
            if (xhr.status === 200 && contentType.indexOf("text/csv") === 0) {
                var disposition = xhr.getResponseHeader("Content-Disposition") || "";
                var match = /filename="?([^";]+)"?/.exec(disposition);
                var link = document.createElement("a");
                link.href = URL.createObjectURL(xhr.response);
                link.download = match ? match[1] : "Transaction-Report.csv";
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                setTimeout(function () { URL.revokeObjectURL(link.href); }, 1000);
                finishReportRequest();
                return;
            }
            if (xhr.status === 200) {
                // Probably the login page, because the session expired
                finishReportRequest();
                alert("${g.message(code: 'report.sessionExpired.message', default: 'Unable to download the report. Your session may have expired, please refresh the page and try again.')}");
                return;
            }
            // Errors are JSON, so read the blob as text to get the message
            var reader = new FileReader();
            reader.onload = function () {
                finishReportRequest();
                alert(getReportErrorMessage(xhr.status, reader.result));
            };
            reader.readAsText(xhr.response);
        };
        xhr.ontimeout = function () {
            finishReportRequest();
            alert(getReportErrorMessage("timeout"));
        };
        xhr.onerror = function () {
            finishReportRequest();
            alert(getReportErrorMessage(xhr.status));
        };
        xhr.send();
    }

    // Shows a quantity in a table cell. Zeros are dimmed, and non-zero quantities get the given class (if any).
    function renderQuantity(cell, value, nonZeroClass) {
        var quantity = Number(value) || 0;
        cell.removeClass('credit debit zero').html(quantity.toLocaleString('en-US'));
        if (quantity === 0) {
            cell.addClass('zero');
        } else if (nonZeroClass) {
            cell.addClass(nonZeroClass);
        }
    }

    // Shows a change in quantity as a credit if it's positive, or as a debit (without the sign) if it's negative
    function renderQuantityChange(cell, value) {
        var quantity = Number(value) || 0;
        renderQuantity(cell, Math.abs(quantity), quantity > 0 ? 'credit' : 'debit');
    }

    // The settings of the report that was run, so that the metadata and the details dialog use them even if the form
    // has been changed since (e.g. with the previous and next period links)
    var lastReportParams = null;

    function getSelectedText(selector) {
        return $(selector).find("option:selected").map(function () { return $.trim($(this).text()); }).get()
            .filter(function (text) { return text; });
    }

    // The form's values, as the parameters of the report request (for the CSV download and the page's URL). Unchecked
    // checkboxes and empty fields are left out.
    function getFormParams() {
        var params = {
            "location.id": $("#locationId").val(),
            startDate: $("#startDate").val(),
            endDate: $("#endDate").val(),
            category: $("#category").val(),
            products: $("#products-select").val(),
            tags: $("#tags").val(),
            catalogs: $("#catalogs").val()
        };
        ["includeCategoryChildren", "includeProductsWithNoActivity", "includeInternalTransfers"].forEach(function (name) {
            if ($("#" + name).is(":checked")) {
                params[name] = true;
            }
        });
        $.each(params, function (name, value) {
            if (value == null || value === "" || ($.isArray(value) && !value.length)) {
                delete params[name];
            }
        });
        return params;
    }

    // The settings of the report, read from the form when the report is run
    function getReportParams() {
        var labels = {
            category: "${g.message(code: 'category.label', default: 'Category')}",
            catalogs: "${g.message(code: 'catalogs.name.label', default: 'Formulary')}",
            tags: "${g.message(code: 'tags.label', default: 'Tags')}",
            products: "${g.message(code: 'products.label', default: 'Products')}",
            subcategories: "${g.message(code: 'report.metadata.withSubcategories.label', default: 'with subcategories')}",
            internalTransfers: "${g.message(code: 'report.metadata.internalTransfers.label', default: 'Internal transfers')}",
            productsWithNoActivity: "${g.message(code: 'report.metadata.productsWithNoActivity.label', default: 'Products with no activity')}"
        };
        var filters = [];
        if ($("#category").val() && $("#category").val() !== "${command?.rootCategory?.id}") {
            filters.push(labels.category + ": " + getSelectedText("#category").join(", ") +
                ($("#includeCategoryChildren").is(":checked") ? " (" + labels.subcategories + ")" : ""));
        }
        [["#catalogs", labels.catalogs], ["#tags", labels.tags], ["#products-select", labels.products]].forEach(function (filter) {
            var selected = getSelectedText(filter[0]);
            if (selected.length) {
                filters.push(filter[1] + ": " + selected.join(", "));
            }
        });
        var included = [];
        if ($("#includeInternalTransfers").is(":checked")) {
            included.push(labels.internalTransfers);
        }
        if ($("#includeProductsWithNoActivity").is(":checked")) {
            included.push(labels.productsWithNoActivity);
        }
        return {
            locationId: $("#locationId").val(),
            locationName: $("#locationId option:selected").text(),
            startDate: $("#startDate").val(),
            endDate: $("#endDate").val(),
            period: $("#reportPeriod").val(),
            includeInternalTransfers: $("#includeInternalTransfers").is(":checked"),
            filters: filters,
            included: included
        };
    }

    // The products in the report to look into. Each one filters the table to those products.
    var REPORT_SUMMARIES = {
        differences: function (row) { return row.Differences !== 0; },
        stockouts: function (row) { return row.Opening > 0 && row.Closing <= 0; },
        negativeClosing: function (row) { return row.Closing < 0; }
    };

    // The filter that a summary count set on the table, or null for none
    var reportRowFilter = null;

    function setReportRowFilter(key) {
        reportRowFilter = (reportRowFilter === key) ? null : key;
        $(".report-metadata-summary a.summary-count").removeClass("active");
        $(".report-metadata-filter-clear").remove();
        if (reportRowFilter) {
            var cell = $(".report-metadata-summary[data-summary='" + reportRowFilter + "']");
            cell.find("a.summary-count").addClass("active");
            cell.append($('<a href="#" class="report-metadata-filter-clear">')
                .text("${g.message(code: 'default.clear.label', default: 'Clear')}")
                .click(function (event) {
                    event.preventDefault();
                    setReportRowFilter(reportRowFilter);
                }));
        }
        $('#transactionReport').dataTable().fnDraw();
    }

    // Shows the details of the report that was just loaded
    function showReportMetadata(json) {
        var seconds = Math.round((Date.now() - reportStartTime) / 1000);
        var rows = (json && json.aaData) || [];
        var none = "${g.message(code: 'default.none.label', default: 'None')}";
        $(".report-metadata-facility").text(lastReportParams.locationName);
        $(".report-metadata-period").text(lastReportParams.period);
        $(".report-metadata-filters").text(lastReportParams.filters.join("; ") || none);
        $(".report-metadata-included").text(lastReportParams.included.join(", ") || none);
        $(".report-metadata-products").text(rows.length.toLocaleString('en-US'));
        reportRowFilter = null;
        $.each(REPORT_SUMMARIES, function (key, test) {
            var matching = rows.filter(test);
            var cell = $(".report-metadata-summary[data-summary='" + key + "']").empty();
            if (!matching.length) {
                cell.append($('<span class="zero">').text("0"));
                return;
            }
            cell.append($('<a href="#" class="summary-count">').text(matching.length.toLocaleString('en-US')).click(function (event) {
                event.preventDefault();
                setReportRowFilter(key);
            }));
            // The net change from counts, for the products with differences
            if (key === "differences") {
                var net = matching.reduce(function (sum, row) { return sum + row.Differences; }, 0);
                cell.append(document.createTextNode(" (" + (net > 0 ? "+" : net < 0 ? "\u2212" : "") +
                    Math.abs(net).toLocaleString('en-US') + ")"));
            }
        });
        $(".report-metadata-generated").text(new Date().toLocaleString());
        $(".report-metadata-duration").text(Math.floor(seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60);
        $(".report-metadata-empty").hide();
        $(".report-metadata-values").show();
    }

	// The DataTables messages. The empty table message depends on whether a report has been run.
	function getDataTableLanguage(emptyTableMessage) {
		return {
			"sEmptyTable": emptyTableMessage,
			"sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
			"sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
					"_START_" +
					" ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
					"_END_" +
					" ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
					"_TOTAL_" +
					" ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
			"sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
			"sZeroRecords": "${g.message(code: 'default.dataTable.noRecordsFound.label', default: 'No records found')}",
			"sProcessing": "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
			"sInfoFiltered": "(${g.message(code: 'default.dataTable.filteredFrom.label', default: 'filtered from')} " +
					"_MAX_" +
					" ${g.message(code: 'default.dataTable.totalEntries.label', default: 'total entries')})"
		};
	}

	function initializeDataTable(validated) {
    	const baseOptions = {
    		"bJQueryUI": true,
			"bDestroy": true,
			"bProcessing": false,  // The report overlay shows that the report is loading
			"iDisplayLength": 100,
			"bScrollCollapse": true,
			"bAutoWidth": false,  // The column widths are set in the styles
			"bScrollInfinite": true,
			"sScrollY": 500,
			"sPaginationType": "two_button",
			"aoColumns": [
				// The column widths are set in the styles
				{"mData": "Code"},
				{"mData": "Name"},
				{"mData": "Category"},
				{"mData": "Opening", "sType": 'numeric', "sClass": "right" },
				{"mData": "Credits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Debits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Differences", "sType": 'numeric', "sClass": "right" },
				{"mData": "Closing", "sType": 'numeric', "sClass": "right" }
			],
			"aLengthMenu": [
				[5, 15, 25, 50, 100, 500, 1000, -1],
				[5, 15, 25, 50, 100, 500, 1000, "All"]
			]
		}
		const dataOptions = {
			"sAjaxSource": "${request.contextPath}/json/getTransactionReport",
			"fnServerParams": function ( data ) {
				data.push({ name: "location.id", value: $("#locationId").val() });
				data.push({ name: "startDate", value: $("#startDate").val() });
				data.push({ name: "endDate", value: $("#endDate").val() });
				data.push({ name: "category", value: $("#category").val() });

                const selectedProducts = $("#products-select").val();
                if (selectedProducts) {
                  selectedProducts.forEach(productId => {
                    data.push({ name: "products", value: productId });
                  });
                }

				data.push({ name: "tags", value: $("#tags").val() });
				data.push({ name: "catalogs", value: $("#catalogs").val() });
				if ($('#includeProductsWithNoActivity').is(':checked')) {
					data.push({ name: "includeProductsWithNoActivity", value: true });
				}
				if ($('#includeInternalTransfers').is(':checked')) {
					data.push({ name: "includeInternalTransfers", value: true });
				}
				if($('#includeCategoryChildren').is(':checked')) {
					data.push({ name: "includeCategoryChildren", value: $("#includeCategoryChildren").val() });
				}
			},
            "fnServerData": function ( sSource, aoData, fnCallback ) {
				$.ajax({
					"dataType": 'json',
					"type": "POST",
					"url": sSource,
					"data": aoData,
					"success": function (json) {
						showReportMetadata(json);
						fnCallback(json);
					},
					"timeout": reportTimeout,
					"complete": finishReportRequest,
					"error": function (xhr, status, error) {
						// User probably refreshed page or clicked on a link, so this isn't really an error
						if (status !== "timeout" && (xhr.readyState == 0 || xhr.status == 0)) {
							return;
						}
						alert(getReportErrorMessage(status === "timeout" ? status : xhr.status, xhr.responseText));
						initializeDataTable(false);
					}
				})
			},
			"oLanguage": getDataTableLanguage("${g.message(code: 'report.transaction.noRows.message', default: 'No products had activity in this period. Check Include products with no activity to see products that had stock.')}"),
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">',
            "fnRowCallback": function (nRow, aData) {
				// The tooltip shows the full name (long names are truncated), or the original name if we display
				// the DisplayName
				$('td:eq(1)', nRow).empty().append($('<div class="clamp">').text(aData?.DisplayName ?? aData?.Name))
					.attr('title', aData?.Name);
				$('td:eq(2)', nRow).empty().append($('<div class="clamp">').text(aData?.Category ?? ''))
					.attr('title', aData?.Category);
				renderQuantity($('td:eq(3)', nRow), aData["Opening"]);
				renderQuantity($('td:eq(4)', nRow), aData["Credits"], 'credit');
				renderQuantity($('td:eq(5)', nRow), aData["Debits"], 'debit');
				renderQuantityChange($('td:eq(6)', nRow), aData["Differences"]);
				renderQuantity($('td:eq(7)', nRow), aData["Closing"]);
				return nRow;
	          }
		};

		// Before a report has been run, show only the column headers and a prompt (no info, search or paging)
		const emptyOptions = {
			"sDom": 'rt',
			"oLanguage": getDataTableLanguage("${g.message(code: 'report.transaction.notRun.message', default: 'Choose a facility and period, then click Run Report.')}")
		};
		const options = validated ? { ...baseOptions, ...dataOptions } : { ...baseOptions, ...emptyOptions }
		$('#transactionReport').dataTable(options);
	}


	// The format of the dates in the report period field, and the separator between them
	var REPORT_PERIOD_FORMAT = "DD/MMM/YYYY";
	var REPORT_PERIOD_SEPARATOR = " - ";

	// Sets the report period. The end date is capped at today.
	function setReportPeriod(startDate, endDate) {
		var today = new Date();
		$("#reportPeriod").data("dateRangePicker").setDateRange(startDate, endDate > today ? today : endDate);
		syncReportPeriod();
	}

	// Fills in the hidden start and end date fields from the report period field, or clears them when it isn't a
	// valid period, so that the run button's checks catch it.
	function syncReportPeriod() {
		var dates = $("#reportPeriod").val().split(REPORT_PERIOD_SEPARATOR);
		var startDate = moment(dates[0], REPORT_PERIOD_FORMAT, true);
		var endDate = moment(dates[1], REPORT_PERIOD_FORMAT, true);
		var valid = dates.length === 2 && startDate.isValid() && endDate.isValid();
		$("#startDate").val(valid ? startDate.format("MM/DD/YYYY") : "");
		$("#endDate").val(valid ? endDate.format("MM/DD/YYYY") : "");
		updateReportPeriodSteps();
	}

	// The length of the period, for the previous and next links: {months: n} or {days: n}. It's set by the shortcut
	// that was picked (so that "this quarter" steps by quarters), and kept when stepping. Otherwise (null), it's
	// worked out from the dates.
	var reportPeriodLength = null;
	var pickedShortcutLength = null;
	var SHORTCUT_LENGTHS = {
		today: { days: 1 }, yesterday: { days: 1 }, last7Days: { days: 7 }, last30Days: { days: 30 }, last90Days: { days: 90 },
		thisMonth: { months: 1 }, lastMonth: { months: 1 }, last3Months: { months: 3 }, last6Months: { months: 6 },
		last12Months: { months: 12 }, thisQuarter: { months: 3 }, lastQuarter: { months: 3 }, thisYear: { months: 12 },
		lastYear: { months: 12 }
	};

	// A period of whole months (from the first of a month to the end of a month) steps by months, anything else by
	// its number of days
	function getReportPeriodLength(startDate, endDate) {
		if (reportPeriodLength) {
			return reportPeriodLength;
		}
		if (startDate.date() === 1 && endDate.isSame(endDate.clone().endOf("month"), "day")) {
			return { months: (endDate.year() - startDate.year()) * 12 + endDate.month() - startDate.month() + 1 };
		}
		return { days: endDate.diff(startDate, "days") + 1 };
	}

	// The period before (direction -1) or after (direction 1) the selected one, of the same length, or null if no period
	// is selected. The end date isn't capped at today here.
	function getSteppedReportPeriod(direction) {
		var startDate = moment($("#startDate").val(), "MM/DD/YYYY", true);
		var endDate = moment($("#endDate").val(), "MM/DD/YYYY", true);
		if (!startDate.isValid() || !endDate.isValid()) {
			return null;
		}
		var length = getReportPeriodLength(startDate, endDate);
		var start, end;
		if (length.months) {
			start = startDate.clone().startOf("month").add(direction * length.months, "months");
			end = start.clone().add(length.months, "months").subtract(1, "days");
		} else {
			start = startDate.clone().add(direction * length.days, "days");
			end = start.clone().add(length.days - 1, "days");
		}
		return { start: start, end: end, length: length };
	}

	// Shows the period each link moves to, and disables the next link when that period would start after today
	function updateReportPeriodSteps() {
		var today = moment().startOf("day");
		[[".report-period-previous", -1], [".report-period-next", 1]].forEach(function (step) {
			var period = getSteppedReportPeriod(step[1]);
			var disabled = !period || period.start.isAfter(today);
			$(step[0]).toggleClass("disabled", disabled).attr("title", disabled ? "" :
				period.start.format(REPORT_PERIOD_FORMAT) + REPORT_PERIOD_SEPARATOR +
				moment.min(period.end, today).format(REPORT_PERIOD_FORMAT));
		});
	}

	function stepReportPeriod(direction) {
		var period = getSteppedReportPeriod(direction);
		if (!period || period.start.isAfter(moment().startOf("day"))) {
			return;
		}
		setReportPeriod(period.start.toDate(), period.end.toDate());
		// Keep stepping by the same length, even if the dates alone would give a different one (e.g. this month, which
		// ends today, steps back to the whole month before)
		reportPeriodLength = period.length;
		updateReportPeriodSteps();
	}

	// Returns the [start, end] dates of a period shortcut (e.g. last month). "Last n days" ends today, and "last n
	// months" is the n whole months before this one.
	function getShortcutPeriod(period) {
		var now = new Date();
		var year = now.getFullYear();
		var month = now.getMonth();
		var today = new Date(year, month, now.getDate());
		var daysAgo = function (days) { return new Date(year, month, now.getDate() - days); };
		var quarterStartMonth = month - month % 3;
		switch (period) {
			case "today": return [today, today];
			case "yesterday": return [daysAgo(1), daysAgo(1)];
			case "last7Days": return [daysAgo(6), today];
			case "last30Days": return [daysAgo(29), today];
			case "last90Days": return [daysAgo(89), today];
			case "thisMonth": return [new Date(year, month, 1), today];
			case "lastMonth": return [new Date(year, month - 1, 1), new Date(year, month, 0)];
			case "last3Months": return [new Date(year, month - 3, 1), new Date(year, month, 0)];
			case "last6Months": return [new Date(year, month - 6, 1), new Date(year, month, 0)];
			case "last12Months": return [new Date(year, month - 12, 1), new Date(year, month, 0)];
			case "thisQuarter": return [new Date(year, quarterStartMonth, 1), today];
			case "lastQuarter": return [new Date(year, quarterStartMonth - 3, 1), new Date(year, quarterStartMonth, 0)];
			case "thisYear": return [new Date(year, 0, 1), today];
			case "lastYear": return [new Date(year - 1, 0, 1), new Date(year - 1, 11, 31)];
		}
	}

	// The picker's shortcuts, in groups. The picker shows them all on one line, so initializeReportPeriod puts each
	// group on its own line with a label.
	var REPORT_PERIOD_SHORTCUTS = [
		{ label: "${g.message(code: 'report.period.days.label', default: 'Days')}", periods: {
			today: "${g.message(code: 'report.period.today.label', default: 'Today')}",
			yesterday: "${g.message(code: 'report.period.yesterday.label', default: 'Yesterday')}",
			last7Days: "${g.message(code: 'report.period.last7Days.label', default: 'Last 7 days')}",
			last30Days: "${g.message(code: 'report.period.last30Days.label', default: 'Last 30 days')}",
			last90Days: "${g.message(code: 'report.period.last90Days.label', default: 'Last 90 days')}"
		}},
		{ label: "${g.message(code: 'report.period.months.label', default: 'Months')}", periods: {
			thisMonth: "${g.message(code: 'report.period.thisMonth.label', default: 'This month')}",
			lastMonth: "${g.message(code: 'report.period.lastMonth.label', default: 'Last month')}",
			last3Months: "${g.message(code: 'report.period.last3Months.label', default: 'Last 3 months')}",
			last6Months: "${g.message(code: 'report.period.last6Months.label', default: 'Last 6 months')}",
			last12Months: "${g.message(code: 'report.period.last12Months.label', default: 'Last 12 months')}"
		}},
		{ label: "${g.message(code: 'report.period.quarters.label', default: 'Quarters')}", periods: {
			thisQuarter: "${g.message(code: 'report.period.thisQuarter.label', default: 'This quarter')}",
			lastQuarter: "${g.message(code: 'report.period.lastQuarter.label', default: 'Last quarter')}"
		}},
		{ label: "${g.message(code: 'report.period.years.label', default: 'Years')}", periods: {
			thisYear: "${g.message(code: 'report.period.thisYear.label', default: 'This year')}",
			lastYear: "${g.message(code: 'report.period.lastYear.label', default: 'Last year')}"
		}}
	];

	function initializeReportPeriod() {
		var customShortcuts = [];
		$.each(REPORT_PERIOD_SHORTCUTS, function (i, group) {
			$.each(group.periods, function (period, name) {
				// The picker can't pick a day after endDate, so cap the shortcut's end date at today
				customShortcuts.push({ name: name, dates: function () {
					pickedShortcutLength = SHORTCUT_LENGTHS[period];
					var dates = getShortcutPeriod(period);
					var today = new Date();
					return [dates[0], dates[1] > today ? today : dates[1]];
				}});
			});
		});

		$("#reportPeriod").dateRangePicker({
			format: REPORT_PERIOD_FORMAT,
			separator: REPORT_PERIOD_SEPARATOR,
			autoClose: true,
			endDate: new Date(),
			startOfWeek: "monday",
			// Reports are about the past, so when the period is in one month, show it with the month before rather than
			// the month after (which is often in the future and can't be picked)
			lookBehind: true,
			showShortcuts: true,
			shortcuts: {},
			customShortcuts: customShortcuts
		}).on("datepicker-change", function () {
			// A shortcut sets the length of the period; dates picked in the calendar don't
			reportPeriodLength = pickedShortcutLength;
			pickedShortcutLength = null;
			syncReportPeriod();
		})
			// Typed periods are read by the picker, but it doesn't fire datepicker-change for them
			.on("change keyup", function () {
				reportPeriodLength = null;
				syncReportPeriod();
			});

		$(".report-period-previous, .report-period-next").click(function (event) {
			event.preventDefault();
			stepReportPeriod($(this).hasClass("report-period-next") ? 1 : -1);
		});

		// Put each group of shortcuts on its own line, with a label. The &nbsp; spacers between them would be left
		// between the groups, so remove them.
		var shortcuts = $("#reportPeriod").data("dateRangePicker").getDatePicker().find(".custom-shortcut");
		shortcuts.parent().contents().filter(function () { return this.nodeType === Node.TEXT_NODE; }).remove();
		var index = 0;
		$.each(REPORT_PERIOD_SHORTCUTS, function (i, group) {
			var count = Object.keys(group.periods).length;
			shortcuts.slice(index, index + count)
				.wrapAll('<div class="shortcut-group"></div>')
				.first().before($('<span class="shortcut-group-label"></span>').text(group.label));
			index += count;
		});

		// Close the picker after a shortcut is picked, like after picking the end date (autoClose)
		shortcuts.find("a").click(function () {
			$("#reportPeriod").data("dateRangePicker").close();
		});

		// Show the period that was submitted, or default to last month, so that the report can be run after only
		// choosing a facility
		var startDate = $("#startDate").val();
		var endDate = $("#endDate").val();
		if (startDate && endDate) {
			setReportPeriod(moment(startDate, "MM/DD/YYYY").toDate(), moment(endDate, "MM/DD/YYYY").toDate());
		} else {
			var lastMonth = getShortcutPeriod("lastMonth");
			setReportPeriod(lastMonth[0], lastMonth[1]);
		}
	}

	$(document).ready(function() {

		$(".loading").hide();

		// Shows only the rows of the summary that was clicked in the metadata box. DataTables is loaded after this
		// script, so this is added here rather than above.
		$.fn.dataTableExt.afnFiltering.push(function (oSettings, aData, iDataIndex) {
			return oSettings.nTable.id !== 'transactionReport' || !reportRowFilter
				|| REPORT_SUMMARIES[reportRowFilter](oSettings.aoData[iDataIndex]._aData);
		});

		initializeDataTable(false);

		initializeReportPeriod();

		// Pressing Enter in a field runs the report, like submitting a form. Enter is left alone in the dropdowns,
		// where it picks an option, and while the date picker is open.
		$(".parameters").on("keydown", "input[type=text]", function (event) {
			if (event.key !== "Enter" || $(this).closest(".chosen-container, .select2-container").length
					|| $(".date-picker-wrapper").is(":visible")) {
				return;
			}
			event.preventDefault();
			$(".submit-button").click();
		});

		// DataTables doesn't resize the columns when the page width changes. Because the header and footer are in
		// their own tables when the table scrolls, they'd no longer line up with the rows.
		var resizeTimer = null;
		$(window).on('resize', function () {
			clearTimeout(resizeTimer);
			resizeTimer = setTimeout(function () {
				$('#transactionReport').dataTable().fnAdjustColumnSizing(false);
			}, 100);
		});

		$(".submit-button").click(function(event){
			event.preventDefault();
			var today = new Date();
			var locationId = $("#locationId").val();
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();
			var validated = true;

			// FIXME Needed basic form validation to prevent some unwanted issues on the backend
			if (!endDate || !startDate || !locationId) {
				alert("All fields are required");
				validated = false
			}

			startDate = Date.parse(startDate);
			endDate = Date.parse(endDate);

			if (startDate > endDate) {
				alert("Start date must occur before end date");
				validated = false
			}

			if (endDate > today) {
				alert("End date must occur on or before today");
				validated = false
			}

			if (validated && startReportRequest()) {
				lastReportParams = getReportParams();
				initializeDataTable(true);
				// Put the report's settings in the URL, so that reloading the page or sharing the link runs it again
				history.replaceState(null, "", window.location.pathname + "?" +
					$.param($.extend(getFormParams(), { run: true }), true));
			}
		});

		$(".download-button").click(function(event) {
			event.preventDefault();
			var queryString = $.param($.extend(getFormParams(), { format: "text/csv" }), true);
			downloadReport('${request.contextPath}/json/getTransactionReport?' + queryString);
		});

		$('#transactionReport tbody').on('click', 'tr', function () {
			// The message in an empty table isn't a product
			if ($(this).find('td.dataTables_empty').length) {
				return;
			}
			var nTds = $('td', this);
			var productCode = $(nTds[0]).text();
			var productName = $(nTds[1]).text();
			// The settings of the report that was run, not the form, which may have been changed since
			var params = {
				productCode: productCode,
				locationId: lastReportParams.locationId,
				startDate: lastReportParams.startDate,
				endDate: lastReportParams.endDate
			};
			if (lastReportParams.includeInternalTransfers) {
				params.includeInternalTransfers = true;
			}

			if (productCode && productName && lastReportParams) {
				var queryString = $.param(params);
				var url = "${request.contextPath}/report/showTransactionReportDialog?" + queryString;
				openModalDialog("#dlgShowDialog", productCode + " " + productName, 1000, null, url);
			}
		});

		// A link to a report (see the submit button) runs it when the page opens
		if (${params.run == 'true'}) {
			$(".submit-button").click();
		}
	});

</script>

    </body>
</html>
