package testutils

import org.pih.warehouse.pages.LoginPage
import org.pih.warehouse.pages.LocationPage
import geb.Browser


class PageNavigator {
    static def UserLoginedAsManagerForBoston(){
          Browser.drive {
            to LoginPage
            loginForm.with{
                username = "manager"
                password = "password"
            }
            submitButton.click()

            at LocationPage

            boston.click()
          }
    }
}
