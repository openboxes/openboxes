

## What is Crowdin?
Crowdin is a cloud-based localization management platform used for translating and managing 
multilingual content in OpenBoxes. It provides tools for collaborative translation, automation, 
and integration with development workflows.

## Configuration
The Crowdin Integration is enabled by default 

```yaml
openboxes:
    locale:
        localizationModeLocale: 'ach'
```


## Customization 

!!! tip

    We don't have a configuration property to disable Crowdin at the moment. In order to disable 
    the Crowdin integration, simpy remove 'ach' from the supported locales and users will be unable
    to enter into Translation Mode. 

    ```yaml
    openboxes:
        locale:
            supportedLocales: ['ar', 'ach', 'de', 'en', 'es', ...]
    
    ```
