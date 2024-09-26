
## Basic Architecture

![Basic Architecture](https://docs.openboxes.com/en/latest/img/technology-soup.png)

This diagram is an attempt to convey the following concepts about OpenBoxes: 

* Basic architecture (i.e. MVC - separation of concerns)  
* Core technologies (e.g. Grails, Spring, Hibernate, REST API etc) 
* Example of deployment technologies that we currently use (MySQL and Tomcat on any hosting 
provider including an on-premise server, Chrome/Firefox as the web client)

The inclusion of the Mobile app and Twilio integration are somewhat aspirational at this time. 
While there are current features that are well-suited for use within a mobile browser, we're 
hoping to improve the UI to be more responsive and mobile-friendly in the near future. 

We are also investigating whether to build a native mobile application using a framework like 
Android or React Native. This application would likely support barcoding and offline data entry.

With respect to Twilio integration, we are planning to implement SMS notifications for stock 
and expiry events.
