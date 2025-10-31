OpenBoxes supports configurable size limits. You might lower the maxFileSize for security reasons 
i.e. to prevent DDoS attacks. You might increase `maxFileSize` to attach high-resolution images 
and documents (say, to a product or shipment) or to upload large datasets through the data import
feature.

## Defaults
The default max file size is 2097152 (~2MB).
```shell
grails:
    controllers:
        upload:
            maxFileSize: 2097152  # 2MB max file size
            maxRequestSize: 2097152  # 2MB max request size
```
## Overriding
If you ever need to increase or decrease the max file size just calculate the bytes for the 
desired max size.

```shell
grails:
    controllers:
        upload:
            maxFileSize: 10485760  # 10MB
            maxRequestSize: 10485760  # 10MB
```

!!! Tip

    In case it's not clear, the unit of measure conversion formula for converting from MB to bytes
    looks like this. 
    ```
    = 2 MB * 1024 Kb/MB * 1024 bytes/kb 
    = 2 * 1024 * 1024 bytes 
    = 2097152 bytes
    ```
    If you wanted to increase the size to 10MB
    ```
    = 10 MB * 1024 kb/MB * 1024 bytes/kb
    = 10 * 1024 * 1024 bytes
    = 10485760 bytes
    ```
    If you want to allow GBs, then multiply by another 1024 (1024 MB/GB)
    ```
    = 2 GB * 1024 MB/GB * 1024 Kb/MB * 1024 bytes/kb 
    = 2 * 1024 * 1024 * 1024 bytes 
    = 2147483648 bytes
    ```



## Considerations
* [OWASP Vulnerability: Unrestricted File Upload](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)

## Related 
* [https://community.openboxes.com/t/cant-upload-photo/892](https://community.openboxes.com/t/cant-upload-photo/892)
* [Grails3 file upload maxFileSize limit](https://stackoverflow.com/questions/29845943/grails3-file-upload-maxfilesize-limit)
* [Can't handle FileUploadBase$FileSizeLimitExceededException ](https://github.com/grails/grails-core/issues/12631)
