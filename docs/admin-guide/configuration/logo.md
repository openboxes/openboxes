OpenBoxes supports white-labeling, allowing organizations to customize branding, logos, and 
certain UI elements to match their identity without modifying the core codebase.

## Configuration 
```yaml
openboxes:
    logo:
        url: "/assets/openboxes_logo_40x40.jpg"
```

## Customization 

### Add context path
There's a bug in v0.9.x that causes the default logo to return a 404 error. This is 
due to the fact that the context path is not being added properly. In order to fix this issue, 
simply add the context path to the logo URL.

```yaml title="Add missing context path"
openboxes.logo.url: "/openboxes/assets/openboxes_logo_40x40.jpg"
```

### Using a Hosted Image
You can customize the logo using an image hosted on another server. 

```yaml
openboxes.logo.url: "https://openboxes.com/img/logo_100.png"
```
### Using a Document

If you don't have a way to host your logo to an external image hosting provider and would like to 
keep the logo within OpenBoxes, you can upload the logo to the Documents data table. 

1. Go to Configuration > Other > Documents
1. Create a new document
2. Upload logo 
2. Copy document URL (i.e. it should look like the one below)

```yaml
openboxes.logo.url: "https://demo.openboxes.com/openboxes/document/download/ff80818194d239ef0194d23f7a4b0000"
```





