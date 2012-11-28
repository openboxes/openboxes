package org.pih.warehouse.product

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Tag;
import org.pih.warehouse.core.User;

import testutils.DbHelper



class ProductServiceIntegrationTests extends GroovyTestCase {
  def product1;
  def product2;
  def product3;
  def product4;
  def product5;
  def product6;
  def group1;
  def group2;
  protected void setUp(){
    product1 =DbHelper.createProductWithGroups("boo floweree 250mg",["Hoo moodiccina","Boo floweree"])
    product2 = DbHelper.createProductWithGroups("boo pill",["Boo floweree"])
    product3 = DbHelper.createProductWithGroups("foo",["Hoo moodiccina"])
    product4 = DbHelper.createProductWithGroups("abc tellon",["Hoo moodiccina"])
    product5 = DbHelper.createProductWithGroups("goomoon",["Boo floweree"])
    product6 = DbHelper.createProductWithGroups("buhoo floweree root",[])
    group1 = ProductGroup.findByDescription("Hoo moodiccina")
    group2 = ProductGroup.findByDescription("Boo floweree")
  }
  
  void test_searchProductAndProductGroup_shouldGetAllProductsUnderMachtedGroups(){
    def service = new ProductService()
    def result = service.searchProductAndProductGroup("floweree")
    assert result.size() == 5
    assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "Boo floweree" && it[0] == product1.id && it[3] == group2.id}
    assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "Hoo moodiccina" && it[0] == product1.id && it[3] == group1.id}
    assert result.any{ it[1] == "boo pill" && it[2] == "Boo floweree" && it[0] == product2.id && it[3] == group2.id}
    assert result.any{ it[1] == "goomoon" &&  it[2] == "Boo floweree" && it[0] == product5.id && it[3] == group2.id}
    assert result.any{ it[1] == "buhoo floweree root" &&  it[2] == null && it[0] == product6.id && it[3] == null}

  }


}
