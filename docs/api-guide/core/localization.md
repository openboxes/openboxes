## Localization API

### List 
Return all localized messages for a given locale (language code only).
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" "https://openboxes.ngrok.io/openboxes/api/localizations?lang=fr" |jsonlint
{
  "messages": {
    "access.accessDenied.label": "Accès Refusé",
    "access.accessDenied.message": "accès à action  <b>{0}<\\/b>n''a pas été accordée à l''utilisateur  <b>{1}<\\/b>. \r\nVeuillez envoyer un email à votre administrateur système <b>{2}<\\/b>.",
    "action.not.found.message": "Action <b>{0}<\\/b> n'a pas été trouvée",
    "admin.applicationVersion.label": "Version de l'application",
    "admin.emailEnabled.label": "Courriel permis",
    "admin.emailSettings.header": "Paramètres de courriel",
    "admin.externalConfigFile.label": "Fichier de configuration externe",
    "admin.generalSettings.header": "Paramètres généraux",
    "admin.grailsVersion.label": "Version Grails",
    "admin.hostname.label": "Nom de l'hôte ",
    "admin.label": "Administration",
    "admin.port.label": "Port",
    "admin.systemProperties.header": "System Properties",
    "admin.title": "Géstion des paramètres",
    "admin.upgrade.label": "Upgrade",
    "attribute.allowOther.label": "Permetter autre",
    "attribute.backToAttributes.link": "Retourner aux attributs",
    "attribute.label": "Attributs de produits",
    "attribute.options.label": "Options",
    ...
  },
  "supportedLocales": [
    "ar",
    "en",
    "fr",
    "de",
    "it",
    "es",
    "pt"
  ],
  "currentLocale": "fr"
}

```


### Read

#### Read localized message with arguments (English)

```
$ curl -b cookies.txt -X GET "https://openboxes.ngrok.io/openboxes/api/localizations/dashboard.greeting.label?args=Justin&args=Boston&lang=en" |jsonlint
{
  "code": "dashboard.greeting.label",
  "currentLocale": "en"
}
```

#### Read localized message with arguments (French)

```
$ curl -b cookies.txt -X GET "https://openboxes.ngrok.io/openboxes/api/localizations/dashboard.greeting.label?args=Justin&args=Boston&lang=fr"
{
  "code": "dashboard.greeting.label",
  "message": "Bonjour, <b>Justin<\\/b>! vous êtes actuellement connecté dans le dépôt de <b>Boston<\\/b>.",
  "currentLocale": "fr"
}
```

#### Read localized message with arguments (Spanish)

```
$ curl -b cookies.txt -X GET "https://openboxes.ngrok.io/openboxes/api/localizations/dashboard.greeting.label?args=Justin&args=Boston&lang=es"
{
  "code": "dashboard.greeting.label",
  "message": "Bienvenido, <b>Justin<\\/b>!<\\/span> Se inicia la sesión en el <b>Boston<\\/b> del almacén",
  "currentLocale": "es"
}
```

### Exceptions

#### List - Bogus Locale 

```
$ curl -b cookies.txt -X GET -H "Accept: application/json" "https://openboxes.ngrok.io/openboxes/api/localizations?lang=ensfsaf" |jsonlint  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
{
  "errorCode": 500,
  "errorMessage": "class path resource [grails-app/i18n/messages_ensfsaf.properties] cannot be resolved to URL because it does not exist"
}
```

### Read - No message found
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" "https://openboxes.ngrok.io/openboxes/api/localizations/invalid.message.code?lang=en" |jsonlint
{
  "errorCode": 500,
  "errorMessage": "No message found under code 'invalid.message.code' for locale 'en'."
}
```

