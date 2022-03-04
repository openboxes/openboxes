import React from 'react';

import PropTypes from 'prop-types';
import { Route } from 'react-router-dom';

import MainLayout from 'components/Layout/MainLayout';


const MainLayoutRoute = ({ path, component: Component }) => (
  <Route
    path={path}
    render={matchProps => (
      <MainLayout>
        <Component {...matchProps} />
      </MainLayout>
  )}
  />
);

export default MainLayoutRoute;

MainLayoutRoute.propTypes = {
  component: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
  path: PropTypes.string.isRequired,
};
