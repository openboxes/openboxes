package org.pih.warehouse.page

import geb.Page

class DashboardPage extends Page {
    static url = ""

    static at = {
        // Dashboard page is now a generic GSP layout that renders a React app
        title == ""
    }
}
