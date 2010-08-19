package org.pih.warehouse.product;

import java.util.Date;

/**
 * A generic product is the idea of something, in the Aristotlean sense
 * (see Theory of Forms).  A generic product might have many formulations,
 * representations, sizes, shapes; but at its core, it's still that one
 * idea of a thing.  For instance, a reclining chair is still just a chair.
 * A blue, medium-sized glove is still just a glove. 
 */
class GenericProduct {

    String name						// the name of generic products needs to be i18n'd'
    GenericType	genericType

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
    static constraints = {
    }
}
