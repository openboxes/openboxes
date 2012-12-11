package org.pih.warehouse.core


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
