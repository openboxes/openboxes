/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

class ProductGloveCommand implements Serializable {

    String name
    String title
    String description
    String application
    String latex
    String powder
    String sterility
    String material
    String size

    Category category

    static constraints = {
        name(nullable: true)
        description(nullable: true)
        application(nullable: false, inList: ["", "Exam", "Surgical", "Utility", "Other", "Not Specified"])
        latex(nullable: false, inList: ["", "Contains Latex", "Latex-Free", "Not Specified"])
        sterility(nullable: false, inList: ["", "Sterile", "Non-Sterile", "Not Specified"])
        powder(nullable: false, inList: ["", "Powdered", "Powder-Free", "Not Specified"])
        material(nullable: false, inList: ["", "Butadiene", "Non-Chloroprene", "Cotton", "Latex", "Lead", "Naugahyde", "Natural Rubber", "Neoprene", "Nitrile", "Nylon", "Plastic", "Polyethylene", "Polyisoprene", "Polyvinyl Chloride", "Synthetic", "Terry Cloth", "Vinyl", "Other", "Not Specified"])
        size(nullable: false)
    }
}
