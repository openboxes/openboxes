package org.pih.warehouse.core

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User


class LocationRoleIntegrationTests extends GroovyTestCase {
  void test_save(){
    def user = User.list().first()
    user.locale = new Locale("en")
    def location = Location.list().first()
    def role = Role.list().first()
    def locationRole = new LocationRole(location: location, role: role)
    user.addToLocationRoles(locationRole)
    assert user.save(flush: true, failOnError: true)
    assert locationRole.id
  }
}
