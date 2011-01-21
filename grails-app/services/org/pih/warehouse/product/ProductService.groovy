package org.pih.warehouse.product

class ProductService {

	
	/**
	 * 
	 * @return
	 */
	
	Category getRootCategory() {
		
		//def rootCategory = Category.findByName("ROOT");
		// OR 
		//def rootCategory = new Category(name: "All Products");
		//rootCategory.categories = Category.findAllByParentCategoryIsNull([sort: "name", order: "asc"]);

		
		def rootCategory;
		def categories = Category.findAllByParentCategoryIsNull();
		if (categories && categories.size() == 1) {
			rootCategory = categories.get(0);
		}
		else {
			rootCategory = new Category();
			rootCategory.categories = [];
			categories.each {
				rootCategory.categories << it;
			}
		}
		return rootCategory;
	}
	
	
	List getCategoryTree() { 
		return Category.list();		
	}
	
	
}
