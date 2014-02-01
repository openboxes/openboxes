<div id="synonyms">
    <table id="synonymTable" class="zebra">
        <thead>
            <tr>
                <th><warehouse:message code="synonym.name.label" default="Synonym"/></th>
                <th><warehouse:message code="synonym.locale.label" default="Locale"/></th>
                <th><warehouse:message code="default.actions.label" default="Actions"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each var="synonym" in="${synonyms}">
                <tr>
                    <td class="middle">
                        ${synonym.name}
                    </td>
                    <td class="middle">
                        ${synonym.locale}
                    </td>
                    <td class="middle">
                        <g:remoteLink controller="product" action="deleteSynonym" update="synonyms" class="button icon trash"
                                    onError="alert('An error occurred while trying to delete this synonym.  Please refresh the page and try again.');"
                                      id="${synonym.id}" params="[productId:product.id]">Delete</g:remoteLink>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${synonyms}">
                <tr>
                    <td class="center empty" colspan="3">
                        <warehouse:message code="default.none.label"/>
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
                        <input name="id" type="hidden" value="${product?.id}" />
                        <input id="synonym" type="text" name="synonym" value="" size="80" class="medium text"/>
                        <button  class="button icon add">${warehouse.message(code:'default.button.add.label')}</button>
                    </g:formRemote>

                </td>

            </tr>

        </tfoot>
    </table>
</div>
<script>
    function onSuccess() {
        $("#synonym").focus();
    }

    function onComplete() {
        $("#synonym").focus();
    }

</script>