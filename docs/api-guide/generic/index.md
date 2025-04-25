

After creating a few API endpoints I got a little tired of writing the same boilerplate code. So I've added 
another endpoint to allow developers to access any of the domain objects as REST resources via a more generic 
boilerplate-y API that we're calling the Generic API. Catchy, eh?


| HTTP Method | Resource                                 | Used For                                                 |
|-------------|------------------------------------------|----------------------------------------------------------
| GET         | /openboxes/api/<b><domainClass\></b>     | Get a list of resource instances                         |
| POST        | /openboxes/api/<b><domainClass\></b>     | Create resource instance(s).                             |
| GET         | /openboxes/api/<b><domainClass\></b>/:id | Get the resource instance identified by the given ID.    |
| UPDATE      | /openboxes/api/<b><domainClass\></b>/:id | Update the resource instance identified by the given ID. |
| DELETE      | /openboxes/api/<b><domainClass\></b>/:id | Delete the resource instance identified by the given ID. |


NOTE: Not all domains can be manipulated in this way due to some issues around data binding. In the 
near future, most of these domains will become part of our REST API endpoints. 
