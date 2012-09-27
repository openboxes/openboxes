package testutils

import org.pih.warehouse.core.User
import org.pih.warehouse.core.Location


class DbHelper {

    static def CreateUserIfRequired(userName, password) {
        def user =  User.findByName(userName)
        if(!user)
          user = new User(username: userName)
        user.password = password
        user.locale = 'en'
        user.warehouse = Location.findByName("Boston Headquarters")
        user.active = true
        user.save(flush: true)
        user
    }

}
