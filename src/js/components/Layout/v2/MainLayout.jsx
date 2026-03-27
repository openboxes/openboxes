import React from 'react';

import { Outlet } from 'react-router-dom';

import Footer from 'components/Layout/Footer';
import Header from 'components/Layout/Header';

const MainLayout = () => (
  <div className="page-layout">
    <Header />
    <main className="page-layout__main">
      <Outlet />
    </main>
    <Footer />
  </div>
);

export default MainLayout;
