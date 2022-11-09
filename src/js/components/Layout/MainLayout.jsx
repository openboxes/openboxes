import React from 'react';

import PropTypes from 'prop-types';

import Footer from 'components/Layout/Footer';
import Header from 'components/Layout/Header';


const MainLayout = ({ children }) => (
  <div className="page page-dashboard">
    <Header />
    <div className="main">{children}</div>
    <Footer />
  </div>
);

export default MainLayout;

MainLayout.propTypes = {
  children: PropTypes.element.isRequired,
};
