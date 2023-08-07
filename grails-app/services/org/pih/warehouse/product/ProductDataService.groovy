package org.pih.warehouse.product

import grails.gorm.services.Query
import grails.gorm.services.Service

@Service(Product)
interface ProductDataService {

    void delete(String id)

    Product save(Product product)

    Product get(String id)

    @Query("""Select p from Product p
              left join fetch p.documents
              left join fetch p.synonyms
              left join fetch p.productCatalogItems pci
              left join fetch pci.productCatalog
              where p.id=$id""")
    Product getWithDocumentsAndSynonymsAndProductCatalog(String id)

}
