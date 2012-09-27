package org.pih.warehouse.pages

import geb.Page

import testutils.Settings

class LoginPage extends Page{
    static url = Settings.baseUrl + "/auth/login"
    static at = { title == "Login"}
    static content ={
        loginForm { $("form")}
        submitButton(to: LocationPage) { $("button", type:"submit")}
    }

}