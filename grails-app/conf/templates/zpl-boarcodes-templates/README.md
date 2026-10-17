These are templates used for generating barcodes through https://labelary.com/

Each of this need to be uploaded as a document in the system (`Zebra template` document type, with document number
like: `barcodeLabel:product`). Then these are rendered with template service and then sent to the labelary API to 
generate the barcode image. 

Templates are rendered by `BeanPropertyTemplateService`: placeholders are plain property paths such as
`${product.productCode}`, `${internalLocation.name}` or `${container.name}`. Groovy expressions, safe navigation
(`?.`) and GSP tags are not supported, so templates uploaded before this change that use `?.` need to be updated
and uploaded again.
