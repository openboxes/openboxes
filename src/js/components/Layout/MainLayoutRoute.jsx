import React from 'react';
import { Route } from 'react-router-dom';
import PropTypes from 'prop-types';
import MainLayout from './MainLayout';

const MainLayoutRoute = ({ component: Component }) => (
  <Route
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
};
