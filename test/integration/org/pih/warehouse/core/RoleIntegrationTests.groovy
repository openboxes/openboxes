package org.pih.warehouse.core

class RoleIntegrationTests extends GroovyTestCase{
  
  void test_getAdmin(){
    assert Role.admin().roleType == RoleType.ROLE_ADMIN
  }

  void test_getManager(){
    assert Role.manager().roleType == RoleType.ROLE_MANAGER
  }

  void test_getBrowser(){
    assert Role.browser().roleType == RoleType.ROLE_BROWSER
  }

}
