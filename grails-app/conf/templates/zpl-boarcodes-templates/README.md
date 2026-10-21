These are templates used for generating barcodes through https://labelary.com/

Each of this need to be uploaded as a document in the system (`Zebra template` document type, with document number
like: `barcodeLabel:product`). Then these are rendered with template service and then sent to the labelary API to 
generate the barcode image. 

Templates are rendered by `BeanPropertyTemplateService`: placeholders are plain property paths such as
`${product.productCode}`, `${internalLocation.name}` or `${container.name}`. Groovy expressions, safe navigation
(`?.`) and GSP tags are not supported, so templates uploaded before this change that use `?.` need to be updated
and uploaded again.

A placeholder can carry a default value: `${product.category.name:-none}` prints `none` when the value is empty,
and `${product.category.name:-}` prints nothing (use it where older templates used `?.`). The default applies when:

- the value is empty: no value, or empty text (`0`, `false` and text made of spaces are values and print as they are);
- a link in the path is empty, for example a product without a category;
- the page passed the label object without a value, for example a product label printed from a page that passes no
  bin (`${location.name:-}`).

A label object the page does not provide at all is still an error, with or without a default; so are names that are
not label objects and properties that do not exist. After an empty link, the default applies only when the rest of the
path exists on the declared property types; otherwise (a misspelled property, an untyped property) the empty link is
reported as it is without a default. When the label object itself is empty, its properties cannot be checked and the
default is printed. The default is printed as written and cannot contain `}`.
Without a default, an empty value prints nothing, and an empty link or an empty label object is reported as an error.
