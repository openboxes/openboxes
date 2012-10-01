/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
