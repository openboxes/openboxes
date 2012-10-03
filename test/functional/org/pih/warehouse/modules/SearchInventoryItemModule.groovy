package org.pih.warehouse.modules

import geb.Module


class SearchInventoryItemModule extends Module{
    static content ={
        searchCriteral{$("input",name:"searchable-name")}
        firstSuggestion{$("ul.ui-autocomplete li.ui-menu-item a").first()}
    }
}
