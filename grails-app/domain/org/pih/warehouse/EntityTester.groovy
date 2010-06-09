package org.pih.warehouse

/**
 * This class is used to test new features to demonstrate behaviors 
 * with respect to scaffolding and GORM 
 *
 */
class EntityTester {

	String name 
	
	Map<Attribute, List<ProductAttributeValue>> productAttributeValues
	
	static hasMany = [categories : Category, 
	                  conditionTypes : ConditionType]
	
}
