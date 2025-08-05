Defines the directory to use for (temporarily) storing uploaded files such as product images, shipment documents, and data imports.

If the uploads folder does not exist, Tomcat will attempt to create it.

## Configuration
The upload directory can be changed via the following property:

```yaml
openboxes:
    uploads:
        location: "uploads"
```

The specified directory is a relative path starting from the base Tomcat directory as defined by the `$CATALINA_HOME` environment variable. This will typically be one of:
- `/var/lib/tomcatX` (where X is the Tomcat version, ex: `/var/lib/tomcat9`) if you've installed via apt
- `/opt/tomcat` if you've installed manually

So for example, if `$CATALINA_HOME=/opt/tomcat` and you are using the default configuration of "uploads", uploads would be created under `/opt/tomcat/uploads`.

The default config option should work for most setups, but you could change it to something like `"openboxes/uploads"` if you're running multiple Tomcat applications/servers and need to distinguish between them.


## Troubleshooting

### Read-only errors
The uploads directory must be a location that Tomcat has permission to write to. If Tomcat does not have write permission to the directory, you will likely see a variation of the following startup error:

```
INFO  --- [main] org.pih.warehouse.core.FileService: Attempting to create directory /var/lib/tomcat9/uploads
ERROR --- [main] org.pih.warehouse.core.FileService: - Directory /var/lib/tomcat9/uploads cannot be created
```

Which can lead to errors like the following when attempting to import data or upload files:

```
Unable to upload file due to exception: uploads/inventory.xls (No such file or directory)
```
or
```
Unable to upload file due to exception:
    java.io.FileNotFoundException:
        /var/lib/tomcat9/uploads/locations.xls (Read-only file system)
```

If you encounter an error like these, add the absolute path to the uploads directory as a ReadWritePaths config option under the Security section in `/etc/systemd/system/multi-user.target.wants/tomcatX.service` (where X is the Tomcat version, ex: `/etc/.../tomcat9.service`).

For example, if `$CATALINA_HOME=/opt/tomcat` and you're using the default configuration of "uploads":

```
# Security
...
ReadWritePaths=/opt/tomcat/uploads/
```

Then restart the Tomcat server.

You may need to create the uploads folder manually.

See this community post for more details: https://community.openboxes.com/t/unable-to-upload-file-due-to-exception-uploads-inventory-xls-no-such-file-or-directory/211/2

### Folder being created in root directory
There is [a known bug](https://github.com/openboxes/openboxes/issues/1351) that can cause the system to try to create the uploads folder under the root directory (/) instead of the base Tomcat directory.

This will likely still succeed when local testing (since running the application locally does not require an external Tomcat instance) but it will fail with the following errors when deployed to a real Tomcat servlet:

```
INFO  core.UploadService  - Find or create uploads directory uploads
INFO  core.FileService  - Attempting to create directory /uploads
ERROR core.FileService  - - Directory /uploads cannot be created
```

If you encounter the above error, change the upload location property to be an absolute path.

For example, if `$CATALINA_HOME=/opt/tomcat`: 

```yaml
openboxes:
    uploads:
        location: "/opt/tomcat/uploads"
```

See this community post for more details: https://community.openboxes.com/t/filenotfoundexception-tp-link-eap245-ac1750-wireless-gigabit-ceil-eap245-jpg-permission-denied/281
