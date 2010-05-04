core.js - Release Notes

*** NOTE ***

This document is not updated with each release.  Changes to
the core.js source are noted in the README
file for each component that comprises this aggregate:

bubbling/bubbling.js
loading/loading.js
dispatcher/dispatcher.js
wizard/wizard.js
translator/translator.js
lighter/lighter.js
tooltips/tooltips.js
accordion/accordion.js
ext/encoding.js

*************

The core.js file rolls up all of the YUI-CMS utility components into a single
file; it includes the following components:

* Bubbling Core
* Loading Mask
* Dispatcher Plugin
* Wizard Plugin
* Translator Plugin
* Lighter Plugin
* Tooltip Manager
* Accordion Manager
* EXT: BASE64 and CRC utility

For implementations that use four or more  of these files, it may prove
more efficient to include core.js as opposed to including separate files
for each component.