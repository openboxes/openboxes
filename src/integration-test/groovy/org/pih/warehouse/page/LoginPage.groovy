package org.pih.warehouse.page

import geb.Page

class LoginPage extends Page {
    static url = "/auth/login"

    static at = {
        title == 'Login'
    }

    static content = {
        loginButton(to: ChooseLocationPage) { $('#loginButton', 0) }
        usernameInputField { $('#username', 0) }
        passwordInputField { $('#password', 0) }
    }

    void login(String username, String password) {
        usernameInputField << username
        passwordInputField << password
        loginButton.click()
    }
}
