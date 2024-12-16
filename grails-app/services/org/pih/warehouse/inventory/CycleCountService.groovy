package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.criterion.Order
import org.hibernate.sql.JoinType
import org.pih.warehouse.core.Location

@Transactional
class CycleCountService {

    List<CycleCountCandidate> getCandidates(CycleCountCandidateFilterCommand command, String facilityId) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        Location facility = Location.read(facilityId)
        // Store added aliases to avoid duplicate alias exceptions for product
        // This could happen when params.searchTerm and e.g. sort by product is applied
        Set<String> usedAliases = new HashSet<>()
        return CycleCountCandidate.createCriteria().list(max: command.max, offset: command.offset) {
            eq("facility", facility)
            if (command.searchTerm) {
                createProductAlias(delegate, usedAliases)
                or {
                    ilike("product.productCode", "%${command.searchTerm}%")
                    ilike("product.name", "%${command.searchTerm}%")
                }
            }
            if (command.categories) {
                usedAliases.add("product")
                createAlias("product", "product", JoinType.INNER_JOIN)
                "in"("product.category", command.categories)
            }
            if (command.internalLocations) {
                or {
                    command.internalLocations.each {
                        ilike("internalLocations", "%${it}%")
                    }
                }
            }
            if (command.dateLastCount) {
                lte("dateLastCount", command.dateLastCount)
            }
            if (command.catalogs) {
                createProductAlias(delegate, usedAliases)
                createAlias("product.productCatalogItems", "productCatalogItems")
                usedAliases.add("productCatalogItems")
                "in"("productCatalogItems.productCatalog", command.catalogs)
            }
            if (command.tags) {
                createProductAlias(delegate, usedAliases)
                createAlias("product.tags", "tags")
                usedAliases.add("tags")
                "in"("tags.id", command.tags.collect { it.id })
            }
            if (command.sort) {
                getCandidatesSortOrder(command.sort, command.order, delegate, usedAliases)
            }
        }
    }

    private static void getCandidatesSortOrder(String sort, String orderDirection, Criteria criteria, Set<String> usedAliases) {
        switch (sort) {
            case "product":
                createProductAlias(criteria, usedAliases)
                criteria.addOrder(getOrderDirection("product.productCode", orderDirection))
                break
            case "dateLastCount":
                criteria.addOrder(getOrderDirection("dateLastCount", orderDirection))
                break
            case "category":
                createProductAlias(criteria, usedAliases)
                criteria.createAlias("product.category", "category", JoinType.INNER_JOIN)
                usedAliases.add("category")
                criteria.addOrder(getOrderDirection("category.name", orderDirection))
                break
            case "abcClass":
                criteria.addOrder(getOrderDirection("abcClass", orderDirection))
                break
            case "quantityOnHand":
                criteria.addOrder(getOrderDirection("quantityOnHand", orderDirection))
                break
            default:
                break
        }
    }

    private static Order getOrderDirection(String sort, String order) {
        if (order == "desc") {
            return Order.desc(sort)
        }
        return Order.asc(sort)
    }

    private static void createProductAlias(Criteria criteria, Set<String> usedAliases) {
        if (!usedAliases.contains("product")) {
            usedAliases.add("product")
            criteria.createAlias("product", "product", JoinType.INNER_JOIN)
        }
    }
}
