package org.pih.warehouse.product

class ProductService {
	
	def grailsApplication

	Category getRootCategory() {
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
	
	List getQuickCategories() {
		List quickCategories = new ArrayList();
		String quickCategoryConfig = grailsApplication.config.inventoryBrowser.quickCategories;
		if (quickCategoryConfig) {
			quickCategoryConfig.split(",").each {
				Category c = Category.findByName(it);
				if (c != null) {
					quickCategories.add(c);
				}
			};
		}
		Category.findAll().each {
			if (it.parentCategory == null && !quickCategories.contains(it)) {
				quickCategories.add(it);
			}
		}
		return quickCategories;
	}
}
