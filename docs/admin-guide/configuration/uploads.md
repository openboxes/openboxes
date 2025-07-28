Defines the directory to use for (temporarily) storing uploaded files such as product images, shipment documents, and data imports.
If the uploads folder does not exist, tomcat will attempt to create it (which will only succeed if tomcat has write permissions in the parent location).

## Configuration

```shell
openboxes:
    uploads:
        location: "uploads"
```

## Customization
The default configuration adds the uploads directory to the root of the server. This is fine for local testing, but in production you'll likely want to override this behaviour to provide a more specific path for the uploads directory.

Something like `"/opt/tomcat/uploads"` would likely work, or `"/opt/tomcat/webapps/openboxes/uploads"` if you have multiple tomcat apps.

!!! note "Reminder"
    The uploads directory must be a location that tomcat has permission to write to. The default `/uploads` directory won't be created automatically since tomcat won't have permission to create a folder here. If you want to use the default configuration, you will need to create the folder yourself manually and grant the tomcat user permissions to write to the folder.

### Related
* https://community.openboxes.com/t/unable-to-upload-file-due-to-exception-uploads-inventory-xls-no-such-file-or-directory/211
* https://community.openboxes.com/t/how-do-i-create-a-new-product/135
* https://community.openboxes.com/t/product-pictures/290
