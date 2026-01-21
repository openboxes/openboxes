package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class CategoryService {

    /**
     * We wanted this option to be configurable via UI, and not in a global config, so
     * the only way to do this is to mutate all categories at once and switch on/off the flag
     * Since this is a rare operation, and not costly, we can afford to do this check and update
     * all categories at once
     * @param assigningParentToProductEnabled
     */
    void updateAssigningParentToProduct(boolean assigningParentToProductEnabled) {
        List<Category> categories = Category.getAll()
        categories.each {
            it.assigningParentToProductEnabled = assigningParentToProductEnabled
        }
    }

    /**
     * Returns true if all categories have assigningParentToProductEnabled set to true
     */
    boolean isAssigningParentToProductEnabled() {
        List<Category> categories = Category.getAll()
        return categories.every { it.assigningParentToProductEnabled }
    }

    List<Map<String, String>> getCategoriesOptions(boolean includeParentCategories = true) {
        return Category.createCriteria().list {
            if (!includeParentCategories) {
                isEmpty("categories")
            }
            ne("name", "")
        }.collect {
            [
                id: it.id,
                label: it.getHierarchyAsString(" > ")
            ]
        }
    }
}
