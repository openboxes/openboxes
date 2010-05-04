The Dispatcher plugin was created to satisfy a very common need of developers 
using the YUI library: dynamic execution of AJAX response content. Typical strategies 
to fulfill this need, like executing the innerHTML property or referencing remote 
scripts, are unreliable due to browser incompatibilities. The Dispatcher plugin 
avoids those problems.

This component do not required the Bubbling Core Object, only YUI Core, YUI Connection 
Manager, YUI Dom and YUI Event. And it guarantees these three points:

    * Dynamic contents (loaded thru the YUI Connection Manager) will be processed 
      by the Dispatcher, and the javascript chunks will be executed (remote and inline scripts)
    * All the CSS will be included in the current document (remove and inline), including path
      correction for images within the inline CSS.
    * All the contents will be included in the DOM's structure before beginning the execution 
      process. This means that you can use references to an element within the container.

Dispatcher supports cross-domains capabilities using the most common technique (proxy) as an 
optional feature, also include two method ("jsLoader" and "cssLoader") for onDemand loading, and an 
internal hashtable to discard multiple execution for the same remote file (css and JS), 
and path-correction for remote files if you need it. 

More information here:
http://bubbling-library.com/eng/api/docs/plugins/dispatcher