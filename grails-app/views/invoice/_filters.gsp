<%@ page import="org.pih.warehouse.core.ActivityCode" %>
<div class="box">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label><warehouse:message code="invoice.partyFrom.label"/></label>
                <g:selectOrganization name="partyFromId"
                                      id="partyFromId"
                                      value="${params.partyFromId}"
                                      noSelection="['':'']"
                                      class="chzn-select-deselect"
                                      disabled="${true}" />
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'invoice.invoiceNumber.label')}</label>
                <g:textField class="text" id="invoiceNumber" name="invoiceNumber" value="${params.invoiceNumber}" style="width:100%"/>
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'invoice.invoiceStatus.label', default: 'Invoice Status')}</label>
                <g:select id="status"
                          name="status"
                          from="${org.pih.warehouse.invoice.InvoiceStatus.list()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.status}"
                          noSelection="['': '']"/>
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="invoice.vendor.label"/></label>
                <g:selectOrganization name="vendor"
                                      id="vendor"
                                      value="${params.vendor}"
                                      noSelection="['null':'']"
                                      class="chzn-select-deselect"/>
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'invoice.invoiceType.label')}</label>
                <g:select id="invoiceTypeCode"
                          name="invoiceTypeCode"
                          from="${org.pih.warehouse.invoice.InvoiceTypeCode.list()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params?.invoiceTypeCode}"
                          noSelection="['': '']"/>
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'invoice.invoiceDate.label', default: 'Invoice Date')}</label>
                <a href="javascript:void(0);" id="clearDate">Clear</a>
                <g:jqueryDatePicker id="dateInvoiced"
                                    name="dateInvoiced"
                                    size="40"
                                    changeMonthAndYear="true"
                                    value="${params.dateInvoiced}"
                                    format="MM/dd/yyyy"/>
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="default.createdBy.label"/></label>
                <g:selectPersonViaAjax id="createdBy"
                                       name="createdBy"
                                       class="ajaxSelect2"
                                       noSelection="['':'']"
                                       value="${params.createdBy}"
                                       data-allow-clear="true"
                                       data-ajax--url="${request.contextPath }/json/findPersonByName"
                                       data-ajax--cache="true"/>
            </div>
            <div class="filter-list-item buttons center">
                <button type="submit" class="button icon search" name="search" value="true">
                    <warehouse:message code="default.search.label"/>
                </button>
                <g:link controller="invoice" action="list" class="button icon reload">
                    <warehouse:message code="default.button.cancel.label"/>
                </g:link>
            </div>
        </div>
    </g:form>
</div>
<script type="text/javascript">
  $(document).ready(function() {
    $("#clearDate")
      .click(function () {
        $('#dateInvoiced-datepicker').datepicker('setDate', null);
      });
  });
</script>
