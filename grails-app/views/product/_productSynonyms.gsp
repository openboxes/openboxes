<%@ page import="org.pih.warehouse.core.SynonymTypeCode" %>
<div id="synonyms">
    <div class="box">
        <g:hasErrors bean="${productInstance}">
            <div class="errors">
                <g:renderErrors bean="${productInstance}" as="list"/>
            </div>
        </g:hasErrors>
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:if test="${flash.error}">
            <div class="errors p-1">${flash.error}</div>
        </g:if>
        <%
            // Prevent flash messages displaying twice if there is no redirect
            flash.error = null
        %>
        <h2><warehouse:message code="product.synonyms.label" default="Synonyms"/></h2>
        <table id="synonymTable" class="zebra">
            <thead>
                <tr>
                    <th><warehouse:message code="synonym.name.label" default="Synonym"/></th>
                    <th><warehouse:message code="synonym.locale.label" default="Locale"/></th>
                    <th><g:message code="synonym.classification.label" default="Classification"/></th>
                    <th><warehouse:message code="default.actions.label" default="Actions"/></th>
                </tr>
            </thead>
            <tbody>
                <g:each var="synonym" in="${productInstance?.synonyms}">
                    <tr>
                        <td class="middle">
                            ${synonym.name}
                        </td>
                        <td class="middle">
                            ${synonym.locale}
                        </td>
                        <td class="middle">
                            ${g.message(code: 'enum.SynonymTypeCode.' + synonym.synonymTypeCode)}
                        </td>
                        <td class="middle">
                            <a href="javascript:void(0);" class="button btn-show-dialog"  data-width="900"
                               data-title="${g.message(code:'default.edit.label', args: [ g.message(code: 'product.synonym.label')])}"
                               data-url="${request.contextPath}/product/editProductSynonymDialog/${synonym?.id}"
                            >
                                <img src="${resource(dir:'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                                <g:message code="default.button.edit.label" />
                            </a>
                            <g:remoteLink controller="product" action="deleteSynonym" update="synonyms" class="button"
                                          onError="alert('An error occurred while trying to delete this synonym.  Please refresh the page and try again.');"
                                          id="${synonym.id}" params="[productId:productInstance.id]">
                                <img src="${resource(dir: 'images/icons/silk', file:'delete.png')}" />&nbsp;
                                ${warehouse.message(code:'default.button.delete.label')}
                            </g:remoteLink>
                        </td>
                    </tr>
                </g:each>
                <g:unless test="${productInstance?.synonyms}">
                    <tr>
                        <td class="padded center empty" colspan="3">

                        </td>
                    </tr>
                </g:unless>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="3">
                        <g:formRemote id="addSynonymToProduct" name="addSynonymToProduct"
                                      update="synonyms" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()"
                                      onError="alert('An error occurred while trying to add a synonym.  Please refresh the page and try again.');"

                                      url="[controller: 'product', action:'addSynonymToProduct']">
                            <input name="id" type="hidden" value="${productInstance?.id}" />
                            <div class="d-flex w-75 gap-8">
                                <g:selectLocale
                                    name="locale"
                                    noSelection="['':'']"
                                    class="chzn-select-deselect"
                                    data-placeholder="${g.message(code: 'synonym.selectLocale.placeholder.label', default: 'Select a locale')}"
                                    value="${inputValues?.locale}"
                                />
                                <g:select
                                    name="synonymTypeCode"
                                    from="${SynonymTypeCode.list()}"
                                    optionValue="${{ g.message(code: "enum.SynonymTypeCode." + it ) }}"
                                    noSelection="['':'']"
                                    data-placeholder="${g.message(code: 'synonym.synonymTypeCode.placeholder.label', default: 'Select a classification')}"
                                    class="chzn-select-deselect"
                                    value="${inputValues?.synonymTypeCode}"
                                />
                                <input
                                    id="synonym"
                                    type="text"
                                    name="synonym"
                                    value="${inputValues?.synonym}"
                                    size="80"
                                    class="medium text"
                                    placeholder="${g.message(code: 'synonym.typeSynonym.placeholder.label', default: 'Type the synonym here')}"
                                />
                                <button  class="button">
                                    <img src="${resource(dir:'images/icons/silk', file:'add.png')}" />&nbsp;
                                    ${warehouse.message(code:'default.button.add.label')}
                                </button>
                            </div>
                        </g:formRemote>
                    </td>
                    <td>
                       <div class="d-flex flex-wrap">
                           <g:link
                               class="button mr-2"
                               controller="product"
                               action="exportSynonymTemplate"
                               params="[productCode: productInstance.productCode]"
                           >
                               <img src="${resource(dir:'images/icons/silk',file: 'page_excel.png')}" />&nbsp;
                                <g:message
                                    code="default.download.label"
                                    args="[ g.message(code: 'default.template.label', 'Template') ]"
                                />
                           </g:link>
                           <g:uploadForm controller="product" action="importProductSynonyms">
                               <button class="button" type="submit">
                                   <img src="${resource(dir:'images/icons/silk', file:'add.png')}" />&nbsp;
                                   <g:message
                                       code="default.import.label"
                                       args="[ g.message(code: 'default.template.label', 'Template') ]"
                                   />
                               </button>
                               <input name="file" type="file" required />
                               <g:hiddenField name="product.id" value="${productInstance.id}"/>
                           </g:uploadForm>
                       </div>
                    </td>

                </tr>

            </tfoot>
        </table>
    </div>
</div>
<script type="text/javascript">
    function onSuccess() {
        $("#synonym").focus();
    }

    function onComplete() {
        $("#synonym").focus();
    }
</script>
