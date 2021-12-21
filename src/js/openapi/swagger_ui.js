import SwaggerUI from 'swagger-ui-dist'
import 'swagger-ui-dist/swagger-ui.css';

const spec = require('../../../grails-app/conf/openapi.yaml');

const ui = SwaggerUI({
    spec,
    dom_id: '#swagger',
});

ui.initOAuth({
    appName: "Swagger UI Webpack Demo",
    // See https://demo.identityserver.io/ for configuration details.
    clientId: 'implicit'
});
