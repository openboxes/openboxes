package org.pih.warehouse.product;

import java.io.Serializable;

class ProductDetailsCommand implements Serializable {
	
	// Google properties
	String id
	String author
	String googleId
	Date published
	Date updated
	String title
	String description
	String content	
	String unitOfMeasure = "ea"
	Integer packageSize = 1
	String packageUnit
	String packageName
	Date creationTime
	Date modificationTime
	String country
	String language
	String link
	String gtin
	String brand
	String condition

	// NDC properties
	String productType
	String packageDescription
	String ndcCode
	String productNdcCode 
	String labelerName
	String strengthNumber
	String strengthUnit
	String pharmClasses
	String dosageForm
	String route
	String proprietaryName
	String nonProprietaryName
	
	
	Category category
	
	Map links = new HashMap();
	List gtins = new ArrayList();
	List images = new ArrayList();

	
	static constraints = {
		id(nullable:true)
		author(nullable:true)
		googleId(nullable:true)
		published(nullable:true)
		updated(nullable:true)
		title(nullable:true)
		description(nullable:true)
		content(nullable:true)
		unitOfMeasure(nullable:true)
		packageSize(nullable:true)
		packageUnit(nullable:true)
		packageName(nullable:true)
		creationTime(nullable:true)
		modificationTime(nullable:true)
		country(nullable:true)
		language(nullable:true)
		gtin(nullable:true)
		link(nullable:true)
		brand(nullable:true)
		condition(nullable:true)
		category(nullable:true)	
		
		
		productType(nullable:true)
		packageDescription(nullable:true)
		ndcCode(nullable:true)
		productNdcCode(nullable:true)
		labelerName(nullable:true)
		strengthNumber(nullable:true)
		strengthUnit(nullable:true)
		pharmClasses(nullable:true)
		dosageForm(nullable:true)
		route(nullable:true)
		proprietaryName(nullable:true)
		nonProprietaryName(nullable:true)
			
	}
}
