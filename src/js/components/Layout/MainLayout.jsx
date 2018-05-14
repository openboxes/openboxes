import React from 'react';
import PropTypes from 'prop-types';
import Navbar from './Navbar';

const MainLayout = ({ children }) => (
  <div className="page page-dashboard">
    <Navbar />
    <div className="main">{children}</div>
  </div>
);

export default MainLayout;

MainLayout.propTypes = {
  children: PropTypes.element.isRequired,
};
