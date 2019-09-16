/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationEditor
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.OrganizationEditor
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.PersonEditor
import org.pih.warehouse.core.ProductCatalogEditor
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.TagEditor
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserEditor
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.inventory.TransactionTypeEditor
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.CategoryEditor
import org.pih.warehouse.product.ProductCatalog
import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry
import org.springframework.beans.propertyeditors.CustomDateEditor

import java.text.SimpleDateFormat

class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {
    void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true))
        registry.registerCustomEditor(Location.class, new LocationEditor())
        registry.registerCustomEditor(Person.class, new PersonEditor())
        registry.registerCustomEditor(User.class, new UserEditor())
        registry.registerCustomEditor(Category.class, new CategoryEditor())
        registry.registerCustomEditor(Organization.class, new OrganizationEditor())
        registry.registerCustomEditor(TransactionType.class, new TransactionTypeEditor())
        registry.registerCustomEditor(Tag.class, new TagEditor())
        registry.registerCustomEditor(ProductCatalog.class, new ProductCatalogEditor())
    }
}



