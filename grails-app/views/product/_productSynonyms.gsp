<%@ page import="org.pih.warehouse.product.SynonymClassification" %>
<div id="synonyms">
    <div class="box">
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
                            ${g.message(code: 'enum.SynonymClassification.' + synonym.classification)}
                        </td>
                        <td class="middle">
                            <g:remoteLink controller="product" action="deleteSynonym" update="synonyms" class="button"
                                        onError="alert('An error occurred while trying to delete this synonym.  Please refresh the page and try again.');"
                                          id="${synonym.id}" params="[productId:productInstance.id]">
                                <img src="${createLinkTo(dir:'images/icons/silk', file:'delete.png')}" />&nbsp;
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
                                    data-placeholder="Select a locale"
                                />
                                <g:select
                                    name="classification"
                                    from="${SynonymClassification.list()}"
                                    optionValue="${{ g.message(code: "enum.SynonymClassification." + it ) }}"
                                    noSelection="['':'']"
                                    data-placeholder="Select a classification"
                                    class="chzn-select-deselect"
                                />
                                <input
                                    id="synonym"
                                    type="text"
                                    name="synonym"
                                    value=""
                                    size="80"
                                    class="medium text"
                                    placeholder="Type the synonym here"
                                />
                                <button  class="button">
                                    <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />&nbsp;
                                    ${warehouse.message(code:'default.button.add.label')}
                                </button>
                            </div>
                        </g:formRemote>

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
