package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class LocationRoleIntegrationTests extends Specification implements DomainUnitTest<User> {

  @Test
  void test_save(){
    when:
    def user = User.list().first()
    user.locale = new Locale("en")
    def location = Location.list().first()
    def role = Role.list().first()
    def locationRole = new LocationRole(location: location, role: role)
    user.addToLocationRoles(locationRole)
    then:
    assert user.save(flush: true, failOnError: true)
    assert locationRole.id
  }
}
