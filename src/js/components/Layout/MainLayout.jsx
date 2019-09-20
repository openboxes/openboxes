import React from 'react';
import PropTypes from 'prop-types';
import Navbar from './Navbar';
import Footer from './Footer';

const MainLayout = ({ children }) => (
  <div className="page page-dashboard">
    <Navbar />
    <div className="main">{children}</div>
    <Footer />
  </div>
);

export default MainLayout;

MainLayout.propTypes = {
  children: PropTypes.element.isRequired,
};
