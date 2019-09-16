package org.pih.warehouse.modules

import geb.Module


class SearchInventoryItemModule extends Module{
    static content ={
        searchCriteral(wait:true){$("input#searchable-suggest")}
        firstSuggestion(wait: true){$("ul.ui-autocomplete li.ui-menu-item a").first()}
    }
}
