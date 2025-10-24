import React from 'react';

import PropTypes from 'prop-types';
import { Route } from 'react-router-dom';

import MainLayoutV2 from 'components/Layout/v2/MainLayoutV2';

const MainLayoutRouteV2 = ({ path, component: Component }) => (
  <Route
    path={path}
    render={(matchProps) => (
      <MainLayoutV2>
        <Component {...matchProps} />
      </MainLayoutV2>
    )}
  />
);

export default MainLayoutRouteV2;

MainLayoutRouteV2.propTypes = {
  component: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
  path: PropTypes.string.isRequired,
};
