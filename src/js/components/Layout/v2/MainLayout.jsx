import React, { Suspense } from 'react';

import { Outlet, useLocation } from 'react-router-dom';

import ChunkLoadErrorBoundary from 'components/ChunkLoadErrorBoundary';
import Footer from 'components/Layout/Footer';
import Header from 'components/Layout/Header';
import Loading from 'components/Loading';

const MainLayout = () => {
  const { pathname } = useLocation();

  return (
    <div className="page-layout">
      <Header />
      <main className="page-layout__main">
        <ChunkLoadErrorBoundary resetKeys={[pathname]}>
          <Suspense fallback={<Loading />}>
            <Outlet />
          </Suspense>
        </ChunkLoadErrorBoundary>
      </main>
      <Footer />
    </div>
  );
};

export default MainLayout;
