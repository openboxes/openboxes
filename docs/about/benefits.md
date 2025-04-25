# Benefits of using OpenBoxes

## Open Source
<div class="grid cards" markdown>

-   :material-open-source-initiative:{ .lg .middle } __Free, open source software__

    ---

    OpenBoxes is entirely free to use. This can be a significant advantage in the face of limited operational resources.

    [:octicons-arrow-right-24: Source Code](https://github.com/openboxes/openboxes)
</div>


## Visibility
<div class="grid cards" markdown>

-   :material-history:{ .lg .middle } __Comprehensive stock history__

    ---
    Not only does OpenBoxes allow you to view your inventory at a glance, it provides a historical record of inventory 
    transactions. This data is invaluable to accurately understand and forecast demand for future planning periods.

-   :material-truck: __Extensive inventory tracking__ 

    ---
    Record different movement types from multiple sources and across different levels of your organization. 
    
    * Receipts: Supplier :material-arrow-right: Warehouse
    * Movements: Warehouse :material-arrow-right: Facility
    * Replenishments: Facility :material-arrow-right: Consumer
    * Returns: Warehouse :material-arrow-left: Facility

-  :material-barcode: __Product traceability__

    ---
    Record lot numbers for items that have an expiry date to help identify stock that has expired or is about to expire. Lot numbers
    also provide a means to locate stock when a manufacturer recalls a certain item. 
   

-   :material-cogs: __Asset tracking and maintenance__ 

    ---
    Record serial numbers for items that require lifecycle management (including assets such as 
    computers, routers, hospital beds). Upload and manage documents for each product (e.g. data sheets, product manuals, hazardous material handling requirements).

-   :material-certificate: __Grant compliance__

    ---

    Certain grants require that inventory be tracked in very specific ways. Using OpenBoxes 
    comprehensively would allow a site to account for the movement of goods bought with grant monies
    from vendor, to country, to facility — and the consumption of those items at a facility level.

</div>

## Configurability

<div class="grid cards" markdown>

-   :fontawesome-solid-sliders:{ .md .middle }__Customizable inventory levels__

    ---
    Set inventory maximum, minimum, and reorder points per product and location. This allows the 
    user to quickly assess if supplies are overstocked, at appropriate stock levels, or in danger of being out of
    stock if not reordered soon. It can also be used to automate replenishment orders.


-   :material-file-word: __Customizable documents__

    --- 
    Ability to design and generate different types of documents (Certificates of Donation, Packing Lists, etc)
    required for shipping with just a few mouse clicks. Documents can also be uploaded to OpenBoxes to allow operational leadership to save all the
    relevant shipping documents to a particular shipment within the system.


</div>

## Extensibility 

<div class="grid cards" markdown>
-   :material-api: __Robust RESTful API__
    
    --- 
    
    OpenBoxes offers a RESTful API, which allows external applications to interact with its core functionalities.
    
-   :material-webhook: __Configurable Webhooks__
    
    --- 
    
    Use webhooks to trigger actions in other systems based on events occurring in OpenBoxes.
    
    
-   :material-webhook: __Integrate with order management (EDI)__
    
    --- 
    
    Provide a high-level picture of future demand for any particular item, integrating with demand forecasting software will allow those processes to be automated and more accurate.
    
-   :material-file-sync: __Integrate with e-commerce systems__
    
    --- 
    
    Streamline order processing and improve customer satisfaction by integrating your e-commerce system
    (i.e. Shopify, WooCommerce, Magento)
    
-   :material-truck-delivery: __Integrate with transport management software__
    
    --- 
    
    Streamline outbound logistics operations and enhance supply chain efficiency by integration with 
    your TMS of choice. OpenBoxes can provide timeline updates on outbound shipment events and can 
    receive updates to capture delays and Proof of Delivery (POD) notifications.
    
-   :material-chart-timeline-variant: __Integrate with forecasting software__
    
    --- 
    
    While the OpenBoxes stock usage history can provide a high-level picture of future demand for any particular item, 
    integrating with demand forecasting software will allow those processes to be automated and more accurate.
</div>

## Offline

<div class="grid cards" markdown>

 -  :material-sync: __Enable offline cabilities using data replication__
    
    ---
    
    While OpenBoxes supports a hierachy of locations, sometimes it's not possible for all locations to access a
    centralized server due to power and Internet limitations at that site. In this case, you can manage multiple server 
    installations (a central OpenBoxes server for most locations with good Internet) and separate OB server for each
    location that does not have a reliable connection to the Internet. Using data replication softare like [SymmetricDS](http://symmetricds.org), 
    you can bi-directionally sync all data changes between each of these servers.

</div>
