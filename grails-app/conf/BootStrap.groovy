
import org.pih.warehouse.Product;
import org.pih.warehouse.User;
import org.pih.warehouse.Warehouse;
import org.pih.warehouse.StockCard;

class BootStrap {

    def init = { servletContext ->

	User user1 = new User(id: 1,
	    email:"demo@pih.org",
	    firstName:"Justin",
	    lastName:"Miranda",
	    role:"Stock Manager",
	    username:"jmiranda",
	    password: "password").save();
	
	Product product1 = new Product(id: 1,
	    upc:"03600029145X",
	    name: "Advil",
	    description: "Ibuprofen 200 mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	)
	product1.save();

 	Product product2 = new Product(id: 2,
	    upc:"03600022425X",
	    category: "Pain Reliever",
	    name: "Tylenol",
	    description: "Acetominophen 325 mg",
	    user: user1,
	    stockCard: new StockCard()
	)
	product2.save();

	Product product3 = new Product(id: 3,
	    upc:"02600058245X",
	    name: "Asprin",
	    description: "Asprin 20mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	)
	product3.save();
    }

     def destroy = {
     }
} 