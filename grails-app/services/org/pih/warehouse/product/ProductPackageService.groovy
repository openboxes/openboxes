package org.pih.warehouse.product

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.ProductPrice

@Transactional
class ProductPackageService {

    ProductSupplier save(ProductPackageCommand command) {
        setPackageData(command)
        setContractPriceData(command.productSupplier, command.contractPricePrice, command.contractPriceValidUntil)
        return command.productSupplier
    }

    private static void setPackageData(ProductPackageCommand command) {
        ProductSupplier productSupplier = command.productSupplier
        productSupplier.minOrderQuantity = command.minOrderQuantity
        productSupplier.tieredPricing = command.tieredPricing

        ProductPackage defaultProductPackage = productSupplier?.productPackages?.find {
            it.uom == command.uom && it.quantity == command.productPackageQuantity
        }

        // If there is no product package with given unit of measure and of given quantity, create one
        if (!defaultProductPackage) {
            defaultProductPackage = new ProductPackage()
            defaultProductPackage.name = "${command.uom?.code}/${command.productPackageQuantity}"
            defaultProductPackage.description = "${command.uom?.name} of ${command.productPackageQuantity}"
            defaultProductPackage.product = command.productSupplier?.product
            defaultProductPackage.uom = command.uom
            defaultProductPackage.quantity = command.productPackageQuantity
            productSupplier.addToProductPackages(defaultProductPackage)
            productSupplier.defaultProductPackage = defaultProductPackage
        }
        // If product package price is not provided, skip assigning pricing data
        if (command.productPackagePrice == null) {
            return
        }
        // If product package price is provided and default product package doesn't have
        // product price yet, create ProductPrice instance and assign it to the package
        if (defaultProductPackage.productPrice == null) {
            ProductPrice productPrice = new ProductPrice(price: command.productPackagePrice)
            defaultProductPackage.productPrice = productPrice
            return
        }
        // If product package price is provided and the default product package already has price,
        // update this price's data
        defaultProductPackage.productPrice.price = command.productPackagePrice
        defaultProductPackage.lastUpdated = new Date()
    }

    private static void setContractPriceData(ProductSupplier productSupplier, BigDecimal contractPricePrice, Date validUntil) {
        ProductPrice contractPrice = productSupplier.contractPrice

        // 1. Contract price doesn't exist, and no data is passed, so we don't need to create a new object
        if (!contractPrice && !contractPricePrice && !validUntil) {
            return
        }

        // 2. Contract price exists, but we want to set the price and date to null, so we can't just delete that object
        if (contractPrice && !contractPricePrice && !validUntil) {
            contractPrice.delete()
            productSupplier.contractPrice = null
            return
        }

        // 3. If the contract price doesn't exist, create a new one
        if (!contractPrice) {
            contractPrice = new ProductPrice()
            productSupplier.contractPrice = contractPrice
        }

        // 4. Set passed data
        contractPrice.price = contractPricePrice
        contractPrice.toDate = validUntil
    }
}
