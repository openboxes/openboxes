package org.pih.warehouse.page

import geb.Page

class DashboardPage extends Page {
    static url = "/"

    static at = {
        title == 'Dashboard'
    }
}
