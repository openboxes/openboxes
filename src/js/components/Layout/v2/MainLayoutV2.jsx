import React from 'react';

import PropTypes from 'prop-types';

import Footer from 'components/Layout/Footer';
import Header from 'components/Layout/Header';

const MainLayoutV2 = ({ children }) => (
  <div className="page-layout">
    <Header />
    <main className="page-layout__main">{children}</main>
    <Footer />
  </div>
);

export default MainLayoutV2;

MainLayoutV2.propTypes = {
  children: PropTypes.element.isRequired,
};
