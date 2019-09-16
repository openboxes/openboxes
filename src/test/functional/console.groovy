import geb.Browser
import java.util.UUID
 
Browser.drive {
    def baseUrl = "http://localhost:8888/openboxes"  //local dev env
    def productName = "test" + UUID.randomUUID().toString()[0..5]
    go baseUrl + "/auth/login"
    assert title == "Login"
    $("input", name:"username").value("manager")
    $("input", name:"password").value("password")
    $("button", type:"submit").click()
    assert title == "Dashboard" 
    
    go baseUrl + "/product/create"
    assert title == "Add new product"
    $("#tabs-details form input", name:"name").value(productName)
    $("#tabs-details form").find("select", name:"category.id").value("2") //supplies(3)
    $("button", type:"submit").click()
    assert title == "Record inventory" 
    
    assert $("div.title").text().trim() == productName
    
          
}