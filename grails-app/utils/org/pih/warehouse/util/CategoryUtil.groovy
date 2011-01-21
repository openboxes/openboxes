package org.pih.warehouse.util

import javax.annotation.Resource;

import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductService;

class CategoryUtil {

	@Resource(name="productService")
	static ProductService productService;
	
	static List listCategories() { 
		productService.getCategoryTree();
	}
	
}
