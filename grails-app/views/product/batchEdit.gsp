
<%@ page import="org.pih.warehouse.core.User; org.pih.warehouse.product.Product" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />

    <title><warehouse:message code="product.batchEdit.label" /></title>

</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${commandInstance}">
            <div class="errors">
                <g:renderErrors bean="${commandInstance}" as="list" />
            </div>
        </g:hasErrors>


        <div class="yui-gf">
            <div class="yui-u first">

                <div class="dialog form box">
                    <h2>
                        <warehouse:message code="default.filters.label"/>
                    </h2>
                    <g:form action="batchEdit" method="get">
                        <g:hiddenField name="max" value="${params.max?:10}"/>
                        <div class="filter">
                            <label><warehouse:message code="category.label"/></label>
                            <div>
                                <g:selectCategory name="categoryId" class="chzn-select-deselect" value="${categoryInstance?.id}"
                                                  data-placeholder=" " style="width: 100%;" noSelection="['null':'']"/>
                            </div>
                            <div>
                                <g:checkBox name="includeCategoryChildren" value="${params?.includeCategoryChildren}"/>
                                <label><warehouse:message code="product.includeCategoryChildren.label" default="Include category children"/></label>
                            </div>
                        </div>
                        <%--
                        <div class="filter">
                            <label><warehouse:message code="product.tags.label"/></label>
                            <div>
                                <g:selectTag name="tag.id" class="chzn-select" value="${tagInstance?.id}"/>
                            </div>
                        </div>
                        --%>
                        <div class="filter">
                            <label><warehouse:message code="product.name.label"/></label>
                            <div>
                                <g:textField name="name" value="${params.name}" class="text" size="40"/>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.productCode.label"/></label>
                            <div>
                                <g:textField name="productCode" value="${params.productCode}" class="text" size="40"/>
                            </div>
                            <div>
                                <g:checkBox name="productCodeIsNull" value="${params?.productCodeIsNull}"/>
                                <label><warehouse:message code="product.productCodeIsNull.label" default="Product code is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.unitOfMeasure.label"/></label>
                            <div>
                                <g:autoSuggestString id="unitOfMeasure" name="unitOfMeasure" size="50" class="text"
                                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                                     value="${params?.unitOfMeasure}"
                                                     placeholder=""/>
                            </div>
                            <div>
                                <g:checkBox name="unitOfMeasureIsNull" value="${params?.unitOfMeasureIsNull}"/>
                                <label><warehouse:message code="product.unitOfMeasureIsNull.label" default="Unit of measure is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.brandName.label"/></label>
                            <div>
                                <g:autoSuggestString id="brandName" name="brandName" size="50" class="text"
                                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                                     value="${params?.brandName}"
                                                     placeholder="e.g. Q-Tip, Kleenex"/>
                            </div>
                            <div>
                                <g:checkBox name="brandNameIsNull" value="${params?.brandNameIsNull}"/>
                                <label><warehouse:message code="product.brandNameIsNull.label" default="Brand name is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.manufacturer.label"/></label>
                            <div>
                                <g:autoSuggestString id="manufacturer" name="manufacturer" size="50" class="text"
                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                     value="${params?.manufacturer}"
                                     placeholder="e.g. Pfizer, Beckton Dickson"/>
                            </div>
                            <div>
                                <g:checkBox name="manufacturerIsNull" value="${params?.manufacturerIsNull}"/>
                                <label><warehouse:message code="product.manufacturerIsNull.label" default="Manufacturer is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.manufacturerCode.label"/></label>
                            <div>
                                <g:autoSuggestString id="manufacturerCode" name="manufacturerCode" size="50" class="text"
                                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                                     value="${params?.manufacturerCode}"
                                                     placeholder=""/>
                            </div>
                            <div>
                                <g:checkBox name="manufacturerCodeIsNull" value="${params?.manufacturerCodeIsNull}"/>
                                <label><warehouse:message code="product.manufacturerCodeIsNull.label" default="Manufacturer code is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.vendor.label"/></label>
                            <div>
                                <g:autoSuggestString id="vendor" name="vendor" size="50" class="text"
                                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                                     value="${params?.vendor}"
                                                     placeholder=""/>
                            </div>
                            <div>
                                <g:checkBox name="vendorIsNull" value="${params?.vendorIsNull}"/>
                                <label><warehouse:message code="product.vendorIsNull.label" default="Vendor is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="product.vendorCode.label"/></label>
                            <div>
                                <g:autoSuggestString id="vendorCode" name="vendorCode" size="50" class="text"
                                                     jsonUrl="${request.contextPath}/json/autoSuggest"
                                                     value="${params?.vendorCode}"
                                                     placeholder=""/>
                            </div>
                            <div>
                                <g:checkBox name="vendorCodeIsNull" value="${params?.vendorCodeIsNull}"/>
                                <label><warehouse:message code="product.vendorCodeIsNull.label" default="Vendor code is empty"/></label>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="default.createdBy.label"/></label>
                            <div>
                                <g:selectUser name="createdById" class="chzn-select" value="${params.createdById}" noSelection="['':'']" style="width:100%;"/>
                            </div>
                        </div>
                        <div class="filter">
                            <label><warehouse:message code="default.updatedBy.label"/></label>
                            <div>
                                <g:selectUser name="updatedById" class="chzn-select" value="${params.updatedById}" noSelection="['':'']" style="width:100%;"/>
                            </div>
                        </div>
                        <div class="buttons center">
                            <button type="submit" class="button"> ${warehouse.message(code: 'default.button.find.label')}</button>
                        </div>
                    </g:form>
                </div>
            </div>
            <div class="yui-u">
                <div class="box">
                    <div>
                        <g:set var="firstResult" value="${(params.offset?Integer.parseInt(params?.offset):0)+1}"/>
                        <g:set var="pageCount" value="${(products?.size()+firstResult)-1}"/>
                        <g:set var="totalCount" value="${products?.totalCount}"/>

                        <h2>
                            <%--
                            <div style="float: right;">
                                <g:render template="actionsBatch"/>
                            </div>
                            --%>
                            <label>
                                <warehouse:message code="default.results.label"/>
                            </label>
                            <g:if test="${commandInstance?.productInstanceList}">
                                <warehouse:message code="default.showingPaginatedResults.message" args="[firstResult, pageCount, totalCount]"/>

                            </g:if>
                            <g:link controller="product" action="batchEdit">
                                <warehouse:message code="default.reset.label" default="Reset"/>
                            </g:link>

                        </h2>



                    </div>
                    <div class="filter">
                        <g:if test="${categoryInstance}">
                            <span class="tag">
                                <label><warehouse:message code="category.label"/></label>
                                ${categoryInstance?.name?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.includeCategoryChildren}">
                            <span class="tag">
                                <label>
                                    <warehouse:message code="product.includeCategoryChildren.label" default="Include category children"/>
                                </label>
                                ${params.includeCategoryChildren}
                            </span>
                        </g:if>
                        <g:if test="${params.name}">
                            <span class="tag">
                                <label><warehouse:message code="product.name.label"/></label>
                                ${params.name?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.unitOfMeasure}">
                            <span class="tag">
                                <label><warehouse:message code="product.unitOfMeasure.label"/></label>
                                ${params.unitOfMeasure?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.productCode}">
                            <span class="tag">
                                <label><warehouse:message code="product.productCode.label"/></label>
                                ${params.productCode?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.manufacturer}">
                            <span class="tag">
                                <label><warehouse:message code="product.manufacturer.label"/></label>
                                ${params.manufacturer?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.brandName}">
                            <span class="tag">
                                <label><warehouse:message code="product.brandName.label"/></label>
                                ${params.brandName?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.manufacturerCode}">
                            <span class="tag">
                                <label><warehouse:message code="product.manufacturerCode.label"/></label>
                                ${params.manufacturerCode?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.vendor}">
                            <span class="tag">
                                <label><warehouse:message code="product.vendor.label"/></label>
                                ${params.vendor?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.vendorCode}">
                            <span class="tag">
                                <label><warehouse:message code="product.vendorCode.label"/></label>
                                ${params.vendorCode?:warehouse.message(code:'default.none.label')}
                            </span>
                        </g:if>
                        <g:if test="${params.productCodeIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.productCodeIsNull.label" default="Product code is empty"/></label>
                                ${params.productCodeIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.unitOfMeasureIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.unitOfMeasureIsNull.label" default="Unit of measure is empty"/></label>
                                ${params.unitOfMeasureIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.brandNameIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.brandNameIsNull.label" default="Brand name is empty"/></label>
                                ${params.brandNameIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.manufacturerIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.manufacturerIsNull.label" default="Manufacturer is empty"/></label>
                                ${params.manufacturerIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.manufacturerCodeIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.manufacturerCodeIsNull.label" default="Manufacturer code is empty"/></label>
                                ${params.manufacturerCodeIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.vendorIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.vendorIsNull.label" default="Vendor is empty"/></label>
                                ${params.vendorIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.vendorCodeIsNull}">
                            <span class="tag">
                                <label><warehouse:message code="product.vendorCodeIsNull.label" default="Vendor code is empty"/></label>
                                ${params.vendorCodeIsNull}
                            </span>
                        </g:if>
                        <g:if test="${params.createdById}">
                            <span class="tag">
                                <label><warehouse:message code="default.createdBy.label"/></label>
                                ${User.get(params.createdById)?.name}
                            </span>
                        </g:if>
                        <g:if test="${params.updatedById}">
                            <span class="tag">
                                <label><warehouse:message code="default.updatedBy.label" /></label>
                                ${User.get(params.updatedById)?.name}
                            </span>
                        </g:if>


                    </div>
                    <div class="clear"></div>

                    <g:form action="batchSave" method="post">
                        <g:hiddenField name="max" value="${params.max?:0}"/>
                        <g:hiddenField name="categoryId" value="${params?.categoryId}"/>
                        <g:hiddenField name="name" value="${params.name}"/>
                        <g:hiddenField name="productCode" value="${params.productCode}"/>
                        <g:hiddenField name="manufacturer" value="${params.manufacturer}"/>
                        <g:hiddenField name="manufacturerCode" value="${params.manufacturerCode}"/>
                        <g:hiddenField name="brandName" value="${params.brandName}"/>
                        <g:hiddenField name="vendor" value="${params.vendor}"/>
                        <g:hiddenField name="vendorCode" value="${params.vendorCode}"/>
                        <g:hiddenField name="unitOfMeasure" value="${params.unitOfMeasure}"/>
                        <g:hiddenField name="includeCategoryChildren" value="${params.includeCategoryChildren}"/>

                        <g:hiddenField name="productCodeIsNull" value="${params.productCodeIsNull}"/>
                        <g:hiddenField name="manufacturerIsNull" value="${params.manufacturerIsNull}"/>
                        <g:hiddenField name="manufacturerCodeIsNull" value="${params.manufacturerCodeIsNull}"/>
                        <g:hiddenField name="vendorIsNull" value="${params.vendorIsNull}"/>
                        <g:hiddenField name="vendorCodeIsNull" value="${params.vendorCodeIsNull}"/>
                        <g:hiddenField name="brandNameIsNull" value="${params.brandNameIsNull}"/>

                        <g:hiddenField name="createdById" value="${params.createdById}"/>
                        <g:hiddenField name="updatedById" value="${params.updatedById}"/>

                        <div class="dialog">
                            <table>
                                <thead>
                                <tr class="odd">
                                    <th valign="top">
                                        <warehouse:message code="product.productCode.label"/></th>
                                    <th valign="top">
                                        <warehouse:message code="product.name.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="product.primaryCategory.label" /></th>
                                    <%--
                                    <th valign="top">
                                        <label for="name"><warehouse:message code="product.productStatus.label" /></th>
                                    --%>
                                    <th valign="top">
                                        <warehouse:message code="product.manufacturer.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="product.manufacturerCode.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="product.brandName.label" /></th>
                                    <%--
                                    <th valign="top">
                                        <label for="name"><warehouse:message code="product.upc.label" /></th>
                                    <th valign="top">
                                        <label for="name"><warehouse:message code="product.ndc.label" /></th>
                                    --%>
                                    <th valign="top">
                                        <warehouse:message code="default.unitOfMeasure.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="product.coldChain.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="default.createdBy.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="default.updatedBy.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="default.dateCreated.label" /></th>
                                    <th valign="top">
                                        <warehouse:message code="default.lastUpdated.label" /></th>
                                </tr>
                                </thead>
                                <tbody>
                                    <g:if test="${products}">

                                        <g:each var="productInstance" in="${products }" status="status">
                                            <tr class="${status%2?'odd':'even' }">
                                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
                                                    <g:hiddenField name="productInstanceList[${status }].id" value="${productInstance?.id}" size="10" class="text" />
                                                    <g:textField name="productInstanceList[${status }].productCode" value="${productInstance?.productCode}" size="5" class="text" />
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'name', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].name" value="${productInstance?.name}" size="40" class="text"/>
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'category', 'errors')}">
                                                    <g:selectCategory name="categoryInstanceList[${status }].id" class="chzn-select" value="${productInstance?.category?.id}"/>
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].manufacturer" value="${productInstance?.manufacturer}" size="15" class="text"/>

                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].manufacturerCode" value="${productInstance?.manufacturerCode}" size="10" class="text"/>

                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].brandName" value="${productInstance?.brandName}" size="10" class="text"/>

                                                </td>
                                                <%--
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].upc" value="${productInstance?.upc}" size="3"/>

                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].ndc" value="${productInstance?.ndc}" size="3"/>

                                                </td>
                                                --%>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
                                                    <g:textField name="productInstanceList[${status }].unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="10" class="text"/>

                                                </td>

                                                <td valign="top" style="text-align: center;" class="${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
                                                    <g:checkBox name="productInstanceList[${status }].coldChain" value="${productInstance?.coldChain}" />
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'createdBy', 'errors')}">
                                                    ${productInstance?.createdBy}
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'updatedBy', 'errors')}">
                                                    ${productInstance?.updatedBy}
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'dateCreated', 'errors')}">
                                                    ${productInstance?.dateCreated}
                                                </td>
                                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'lastUpdated', 'errors')}">
                                                    ${productInstance?.lastUpdated}
                                                </td>

                                            </tr>

                                        <%--
                                        <g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
                                            <tr class="prop">
                                                <td valign="top" class="name"><label for="attributes">
                                                ${attribute.name }
                                                </label> <g:hiddenField name="attributes[${status }].attribute.id"
                                                    value="${attribute?.id }" /></td>
                                                <td valign="top" class="value">
                                                    <g:if test="${attribute.options }">
                                                        <g:select
                                                            name="attributes[${status }].value" from="${attribute?.options}"
                                                            value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }" />

                                                        <g:if test="${attribute.allowOther}">
                                                            <g:textField name="attributes[${status }].otherValue" value="" />
                                                        </g:if>
                                                    </g:if>
                                                    <g:else>
                                                        <g:textField name="attributes[${status }].value"
                                                            value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }"/>
                                                    </g:else>
                                                </td>
                                            </tr>
                                        </g:each>
                                        --%>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <tr>
                                            <td colspan="11" class="center empty">
                                                <warehouse:message code="default.noResults.message" default="No results"/>
                                            </td>
                                        </tr>
                                    </g:else>
                                </tbody>
                            </table>
                        </div>
                        <div class="paginateButtons">
                            <div class="left">
                                <g:paginate total="${products?.totalCount}" params="['categoryId':params?.categoryId]" max="${params.max}"/>
                            </div>
                            <div class="right">
                                <warehouse:message code="batchEdit.resultsPerPage.label" default="Results per page"/>:
                                <g:if test="${params.max != '5'}"><g:link action="batchEdit" params="[max:5]">5</g:link></g:if><g:else><span class="currentStep">10</span></g:else>
                                <g:if test="${params.max != '10'}"><g:link action="batchEdit" params="[max:10]">10</g:link></g:if><g:else><span class="currentStep">10</span></g:else>
                                <g:if test="${params.max != '25'}"><g:link action="batchEdit" params="[max:25]">25</g:link></g:if><g:else><span class="currentStep">25</span></g:else>
                                <g:if test="${params.max != '50'}"><g:link action="batchEdit" params="[max:50]">50</g:link></g:if><g:else><span class="currentStep">50</span></g:else>
                                <g:if test="${params.max != '100'}"><g:link action="batchEdit" params="[max:100]">100</g:link></g:if><g:else><span class="currentStep">100</span></g:else>
                                <g:if test="${params.max != '-1'}"><g:link action="batchEdit" params="[max:-1]">${warehouse.message(code:'default.all.label') }</g:link></g:if><g:else><span class="currentStep">${warehouse.message(code:'default.all.label') }</span></g:else>
                            </div>
                        </div>

                        <g:if test="${products }">
                            <div class="buttons">

                                <button type="submit" class="button">
                                    ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                                </button>
                                &nbsp;
                                <g:link controller="inventory" action="browse">
                                    ${warehouse.message(code: 'default.button.cancel.label')}
                                </g:link>
                            </div>
                        </g:if>

                    </g:form>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
