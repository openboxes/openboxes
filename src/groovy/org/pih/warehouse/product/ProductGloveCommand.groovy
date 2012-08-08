package org.pih.warehouse.product;
import java.io.Serializable;

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
		name(nullable:true)
		description(nullable:true)
		application(nullable:false, inList: ["", "Exam", "Surgical", "Utility", "Other", "Not Specified"])
		latex(nullable:false, inList: ["", "Contains Latex", "Latex-Free", "Not Specified"])
		sterility(nullable:false, inList: ["", "Sterile", "Non-Sterile", "Not Specified"])
		powder(nullable:false, inList: ["", "Powdered", "Powder-Free", "Not Specified"])
		material(nullable:false, inList: ["", "Butadiene", "Non-Chloroprene", "Cotton", "Latex", "Lead", "Naugahyde", "Natural Rubber", "Neoprene", "Nitrile", "Nylon", "Plastic", "Polyethylene", "Polyisoprene", "Polyvinyl Chloride", "Synthetic", "Terry Cloth", "Vinyl", "Other", "Not Specified"])
		//size(inList: ["Exam", "Surgical", "Utility", "Other", "Not Specified"])
		size(nullable:false)
	}
}
