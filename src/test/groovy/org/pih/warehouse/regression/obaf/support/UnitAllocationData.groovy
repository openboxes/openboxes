package org.pih.warehouse.regression.obaf.support

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

/**
 * In-memory (never saved) domain objects for the allocation unit specs. Activities are set on
 * the Location itself, which is what Location.supports() reads first, so no LocationType is needed.
 *
 * InventoryLevel.findPreferredBinLocation is a GORM `where` query; the unit specs replace it
 * through the metaclass ({@link #preferredBin}) and must call {@link #resetPreferredBin} in cleanup.
 */
class UnitAllocationData {

    private static int seq = 0

    static String nextId(String prefix) {
        return "${prefix}-${++seq}".toString()
    }

    static Location facility(String name = 'Facility', Collection<ActivityCode> activities = [ActivityCode.MANAGE_INVENTORY]) {
        Location l = new Location(name: name, active: true)
        l.id = nextId('fac')
        l.supportedActivities = activities*.id as Set<String>
        return l
    }

    /** A bin under {@code parent}; PICK_STOCK is added unless {@code allocable} is false. */
    static Location bin(Location parent, String name, Collection<ActivityCode> extra = [], boolean allocable = true) {
        Location l = new Location(name: name, active: true, parentLocation: parent)
        l.id = nextId('bin')
        Set<String> activities = (extra*.id as Set<String>)
        if (allocable) {
            activities << ActivityCode.PICK_STOCK.id
            activities << ActivityCode.PUTAWAY_STOCK.id
        }
        l.supportedActivities = activities
        return l
    }

    static Location displayBin(Location parent, String name) {
        return bin(parent, name, [ActivityCode.DISPLAY_STOCK])
    }

    static Product product(String code = null) {
        Product p = new Product(name: "Product ${code}", productCode: code ?: nextId('P'))
        p.id = nextId('prod')
        return p
    }

    static InventoryItem lot(Product product, String lotNumber, Date expirationDate = null) {
        InventoryItem i = new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
        i.id = nextId('ii')
        return i
    }

    static AvailableItem available(InventoryItem lot, Location bin, Number quantityAvailable, Number quantityOnHand = null) {
        return new AvailableItem(inventoryItem: lot, binLocation: bin,
                quantityAvailable: quantityAvailable as BigDecimal,
                quantityOnHand: (quantityOnHand ?: quantityAvailable) as BigDecimal)
    }

    static Date days(int n) {
        return new Date(System.currentTimeMillis() + n * 86400000L)
    }

    /** Makes InventoryLevel.findPreferredBinLocation(facility, product) answer {@code preferred}. */
    static void preferredBin(Location preferred) {
        InventoryLevel.metaClass.static.findPreferredBinLocation = { Location f, Product p -> preferred }
        InventoryLevel.metaClass.static.getPutawayLocations = { Location f, Product p -> preferred ? [preferred] : [] }
    }

    static void resetPreferredBin() {
        GroovySystem.metaClassRegistry.removeMetaClass(InventoryLevel)
    }
}
