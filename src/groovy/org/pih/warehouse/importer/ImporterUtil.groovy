package org.pih.warehouse.importer

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pih.warehouse.product.Category;
import org.springframework.validation.Errors;

class ImporterUtil {

	

	static DateFormat EXCEL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

	
	static getProductService() { 
		return ApplicationHolder.getApplication().getMainContext().getBean("productService")
	}
		
	/**
	 *
	 * @param categoryName
	 * @param errors
	 * @return
	 */
	static Category findOrCreateCategory(String categoryName, Errors errors) {
		def category = Category.findByName(categoryName);
		if (!category) {
			//category = new Category(name: importParams.category, parentCategory: parentCategory);
			category = new Category(name: categoryName, parentCategory: getProductService().getRootCategory());
			if (!category.validate()) {
				category.errors.allErrors.each {
					errors.addError(it);
				}
			}
			else { 
				category.save(failOnError: true)
			}
			//log.debug "Created new category " + category.name;
		}
		return category;
	}


	/**
	 * 
	 * @param expirationDate
	 * @param errors
	 * @return
	 */
	static Date parseDate(Object expirationDate, Errors errors) {
		if (expirationDate) {
			//log.info "expiration date: " + expirationDate
			// If we're passed a date, we can just set the expiration
			if (expirationDate instanceof org.joda.time.LocalDate) {
				expirationDate = expirationDate.toDateMidnight().toDate();
			}
			else {
				try {

					expirationDate = EXCEL_DATE_FORMAT.parse(expirationDate);
				} catch (ParseException e) {
					errors.reject("Could not parse date " + expirationDate + " " + e.getMessage() + ".  Expected date format: yyyy-MM-dd");
				}
			}
		}

	}
}
