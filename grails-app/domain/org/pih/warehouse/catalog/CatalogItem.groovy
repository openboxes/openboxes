package org.pih.warehouse.catalog

import org.pih.warehouse.catalog.Catalog;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Tag;
import org.pih.warehouse.product.Category;

class CatalogItem {

	String name
	String description
	String imageUrl
	
	Product product
	Float price
	Date dateCreated
	Date lastUpdated

	static constraints = {
		name(blank:false, maxSize:128)
		description(maxSize:2000)
		//price(min:0)
	}

	static mapping = {
		//cache usage:"transactional"
		//tags cache:"transactional", cascade: "all,delete-orphan" // sort: "tag"
	}

	static hasMany = [tags : Tag]

	static belongsTo = [ catalog : Catalog ]
	
	static searchable = true

	static List findAllByTag(String tag, Map params) {
		CatalogItem.createCriteria().list {
			tags {
				eq("tag", params.tag)
			}
			maxResults(params.max?.toInteger() ?: 10)
			firstResult(params.offset?.toInteger() ?: 0)
			order(params.sort ?: "name", params.order ?: "asc")
			cacheable(true)
		}
	}

	static int countByTag(String tag) {
		CatalogItem.createCriteria().get {
			tags {
				eq("tag", tag)
			}
			projections {
				count("id")
			}
			cacheable(true)
		}
	}

	static List findAllByCategory(Category category, params) {
		CatalogItem.createCriteria().list {
			product {
				eq("category", category)
			}
			maxResults(params.max?.toInteger() ?: 10)
			firstResult(params.offset?.toInteger() ?: 0)
			order(params.sort ?: "name", params.order ?: "asc")
			cacheable(true)
		}
	}

	static int countByCategory(Category category) {
		CatalogItem.createCriteria().get {
			product {
				eq("category", category)
			}
			projections {
				count("id")
			}
			cacheable(true)
		}
	}

	void addRating(int score) {
		totalScore += score
		numberOfVotes += 1
	}

	double averageRating() {
		totalScore > 0 ? totalScore/numberOfVotes : 0.0
	}
	
	String tagsAsString() {
		tags ? tags.collect {it.tag}.sort().join(" ") : ""
	}


}