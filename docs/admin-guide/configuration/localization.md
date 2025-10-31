## What is a Localization?

Localization (L10n) is the process of adapting software to meet the language, 
cultural, and regional preferences of a specific target audience. It goes beyond just translation 
and includes adjusting date/time formats, number formats, currency symbols, and even UI layouts to 
align with local conventions.

A Locale is made up of a language code and optionally a country code. For example "en_US" is the 
code for US English, whilst "en_GB" is the code for British English.

## Localization Configuration

* Language Translation – Converting text into the target language.
* Date & Time Formats – Adjusting formats based on locale (MM/dd/yyyy vs. dd/MM/yyyy).
* Number & Currency Formatting – Using region-specific separators (1,000.00 vs. 1.000,00).
* Measurement Conversion – Converting between metric (kg, km) and imperial (lbs, miles) systems.

## Default Locale 

The default system locale. This setting controls the default language as well as the default 
date and number formats used within the application.  

```yaml
openboxes:
    locale:
        defaultLocale: 'en'
```


## Supported Locales

List of all supported locales available in the system. 

```yaml
openboxes:
    locale:
        supportedLocales: ['ar', 'ach', 'de', 'en', 'es', ...]

```

!!! note
    In order to enable the CrowdIn integration please keep the 'ach' key (Acholi) in the 
    Supported Locales list configuration.

### Related
* [Adding support for Dutch language](https://community.openboxes.com/t/language-dutch-crowdin/777)



## Default Currency 

    
```yaml
openboxes:
    locale:
        defaultCurrencyCode: "USD"
        defaultCurrencySymbol: '\$'
```

## Supported Currency 
This configuration property is not currently used. 

```shell
openboxes:
    locale:
        supportedCurrencyCodes: ["USD", "CAD", "EUR", "GBP"]

```

## Enable Custom Translations [deprecated]
This allows for the application to store and retrieve custom localization message (messages.properties)
from the database. For performance reasons, we have deprecated this feature. In its place, we 
now integrate CrowdIn to allow for custom translations.

```yaml
openboxes: 
    locale:
        custom:
            enabled: false
```

