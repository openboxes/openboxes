<%@ page import="org.pih.warehouse.core.EntityTypeCode; org.pih.warehouse.product.ProductField; org.pih.warehouse.inventory.InventoryLevel; org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />

        <g:if test="${productInstance?.id}">
	        <title>${productInstance?.productCode } ${productInstance?.name }</title>
		</g:if>
		<g:else>
	        <title><warehouse:message code="product.add.label" /></title>
			<content tag="label1"><warehouse:message code="inventory.label"/></content>
		</g:else>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
        <style>
        .ui-widget button { font-size: 11px; }
        </style>

    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="errors">${flash.error}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>

   			<g:if test="${productInstance?.id }">
				<g:render template="summary" model="[productInstance:productInstance]"/>
			</g:if>
            <g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${locationInstance?.isAccountingRequired()}"/>
            <div class="tabs tabs-ui">
                <ul>
                    <li>
                        <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productDetails&renderNotFoundError=false&${request.queryString}">
                            <g:message code="product.details.label"/>
                        </a>
                    </li>
                    <%-- Only show these tabs if the product has been created --%>
                    <g:if test="${productInstance?.id }">
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productSuppliers">
                                <g:message code="product.productSuppliers.label" default="Sources"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=inventoryLevels">
                                <g:message code="inventoryLevels.label" default="Stock levels"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=documents">
                                <g:message code="product.documents.label" default="Documents"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productSubstitutions">
                                <g:message code="product.substitutions.label" default="Substitutions"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productPackages">
                                <g:message code="packages.label" default="Packages"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productCatalogs">
                                <warehouse:message code="product.catalogs.label" default="Catalogs"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productGroups">
                                <warehouse:message code="product.productGroups.label" default="Product Groups"/>
                            </a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productSynonyms">
                                <g:message code="product.synonyms.label"/>
                            </a>
                        </li>
                        <g:if test="${grailsApplication.config.openboxes.bom.enabled}">
                            <li>
                                <a href="${request.contextPath}/product/renderTemplate/${productInstance?.id}?templateName=productComponents">
                                    <g:message code="product.components.label" default="Components (Bill of Materials)"/>
                                </a>
                            </li>
                        </g:if>
                    </g:if>
                </ul>
            </div>
        </div>
    </div>
    <div class="loading">Loading...</div>
    <script type="text/javascript">

            function validateForm()  {
                var glAccount = $("#glAccount").val();
                var isAccountingRequired = ($("#isAccountingRequired").val() === "true");
                if (isAccountingRequired && (!glAccount || glAccount === "null")) {
                    $("#glAccountLabel").notify("Required");
                    return false;
                } else {
                    return true;
                }
            }

	    	$(document).ready(function() {
              $(".loading")
              .hide();

              $(".tabs")
              .tabs(
                {
                  cookie: {
                    // store cookie for a day, without, it would be a session cookie
                    expires: 1
                  },
                  ajaxOptions: {
                    error: function (xhr, status, index, anchor) {
                      var errorMessage = "Error loading tab: " + xhr.status + " "
                        + xhr.statusText;
                      // Attempt to get more detailed error message
                      if (xhr.responseText) {
                        var json = JSON.parse(xhr.responseText);
                        if (json.errorMessage) {
                          errorMessage = json.errorMessage
                        }
                      }
                      // Display error message
                      $(anchor.hash)
                      .text(errorMessage);

                      // Reload the page if session has timed out
                      if (xhr.statusCode == 401) {
                        window.location.reload();
                      }
                    },
                    beforeSend: function () {
                      $('.loading')
                      .show();
                    },
                    complete: function () {
                      $(".loading")
                      .hide();
                    }
                  }
                }
              );

              $(".open-dialog")
              .livequery('click', function (event) {
                event.preventDefault();
                var id = $(this)
                .attr("dialog-id");
                $("#" + id)
                .dialog({
                  autoOpen: true,
                  modal: true,
                  width: 800
                });
              });
              $(".close-dialog")
              .livequery('click', function (event) {
                event.preventDefault();
                var id = $(this)
                .attr("dialog-id");
                $("#" + id)
                .dialog('close');
              });

              $(".attributeValueSelector").livequery('change', function() {
                if ($(this)
                .val() == '_other') {
                  $(this)
                  .parent()
                  .find(".otherAttributeValue")
                  .show();
                } else {
                  $(this)
                  .parent()
                  .find(".otherAttributeValue")
                  .val('')
                  .hide();
                }
              });

              function updateBinLocation() {
                $("#binLocation")
                .val('updated')
              }

              $(".binLocation").livequery('change', function() {
                updateBinLocation()
              });

              var prevProdType = $('#productType')
              .val();

              $('#productType').livequery('change', function() {
                var currentProdType = $(this)
                .val();

                var response = JSON.parse($.ajax({
                  url: "${request.contextPath}/json/checkIfProductFieldRemoved",
                  type: "get",
                  async: false,
                  data: {
                    oldTypeId: prevProdType,
                    newTypeId: currentProdType
                  },
                  contentType: "text/json",
                  dataType: "json"
                }).responseText);

                if (response && response.fieldRemoved) {
                  var success = confirm(
                    '${warehouse.message(code: 'product.productType.confirmChange.message', default: 'Changing the product type will delete data you have entered for this product. Would you like to proceed?')}');
                  if (success) {
                    prevProdType = $(this)
                    .val();
                  } else {
                    $(this)
                    .val(prevProdType);
                  }
                }

                var data = $('form[name ="productForm"]')
                .serialize();
                window.location = '${g.createLink(controller: 'product', action: productInstance?.id ? 'edit' : 'create')}?'
                  + data;
              });
            });
		</script>
    </body>
</html>
