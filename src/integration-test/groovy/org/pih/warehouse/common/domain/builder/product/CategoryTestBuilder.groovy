package org.pih.warehouse.common.domain.builder.product

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.product.Category

class CategoryTestBuilder extends TestBuilder<Category> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: "Test Category",
                description: "A category to be used by tests. Can be deleted safely.",
        ] as Map<String, Object>
    }

    CategoryTestBuilder name(String name) {
        args.name = name
        return this
    }

    CategoryTestBuilder parentCategory(Category parentCategory) {
        args.parentCategory = parentCategory
        return this
    }

    CategoryTestBuilder rootCategory() {
        return parentCategory(null)
    }
}
