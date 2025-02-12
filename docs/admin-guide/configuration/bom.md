The Bill of Materials (BOM) feature in OpenBoxes allows users to define component lists for 
assembled products, kits, or repackaged items. Eventually,  supports hierarchical structures, enabling 
automated inventory deductions when creating finished goods. BOMs streamline stock management by 
ensuring accurate tracking of raw materials and finished products during fulfillment and 
replenishment processes.


!!! note
    When enabled, the Bill of Materials feature allows you to define a parent product with 
    multiple child products (i.e. components). You can also upload instructions needed to 
    produce the product. However, as of v0.9.x, we have not yet implemented any features to 
    take 


## Configuration
This property enables or disables the BOM functionality. Additional properties may control specific 
behaviors, such as automatic inventory deduction, default unit conversions, or validation rules.

```shell
openboxes:
  bom:
    enabled: false
```
