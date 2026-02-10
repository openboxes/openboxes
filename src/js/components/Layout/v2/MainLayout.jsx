import React from 'react';

import PropTypes from 'prop-types';

import Footer from 'components/Layout/Footer';
import Header from 'components/Layout/Header';

const MainLayout = ({ children }) => (
  <div className="page-layout">
    <Header />
    <main className="page-layout__main">{children}</main>
    <Footer />
  </div>
);

export default MainLayout;

MainLayout.propTypes = {
  children: PropTypes.element.isRequired,
};
