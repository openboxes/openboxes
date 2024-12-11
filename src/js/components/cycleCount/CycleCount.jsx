import React from 'react';

import CycleCountAllProducts from 'components/cycleCount/CycleCountAllProducts';
import CycleCountHeader from 'components/cycleCount/CycleCountHeader';
import CycleCountToApprove from 'components/cycleCount/CycleCountToApprove';
import CycleCountToCount from 'components/cycleCount/CycleCountToCount';
import CycleCountToResolve from 'components/cycleCount/CycleCountToResolve';
import Tabs from 'components/listPagesUtils/Tabs';
import {
  ALL_PRODUCTS_TAB,
  TO_APPROVE_TAB,
  TO_COUNT_TAB,
  TO_RESOLVE_TAB,
} from 'consts/cycleCount';
import useQueryParams from 'hooks/useQueryParams';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const CycleCount = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: ALL_PRODUCTS_TAB });
  useTranslation('cycleCount');

  const tabs = {
    [ALL_PRODUCTS_TAB]: {
      label: {
        id: 'react.cycleCount.allProducts.label',
        defaultMessage: 'All products',
      },
      onClick: (tab) => switchTab(tab),
    },
    [TO_COUNT_TAB]: {
      label: {
        id: 'react.cycleCount.toCount.label',
        defaultMessage: 'To count',
      },
      onClick: (tab) => switchTab(tab),
    },
    [TO_RESOLVE_TAB]: {
      label: {
        id: 'react.cycleCount.toResolve.label',
        defaultMessage: 'To resolve',
      },
      onClick: (tab) => switchTab(tab),
    },
    [TO_APPROVE_TAB]: {
      label: {
        id: 'react.cycleCount.toApprove.label',
        defaultMessage: 'To approve',
      },
      onClick: (tab) => switchTab(tab),
    },
  };

  const parsedQueryParams = useQueryParams();

  return (
    <PageWrapper>
      <CycleCountHeader />
      <Tabs config={tabs} />
      {parsedQueryParams.tab === ALL_PRODUCTS_TAB && <CycleCountAllProducts />}
      {parsedQueryParams.tab === TO_COUNT_TAB && <CycleCountToCount />}
      {parsedQueryParams.tab === TO_RESOLVE_TAB && <CycleCountToResolve />}
      {parsedQueryParams.tab === TO_APPROVE_TAB && <CycleCountToApprove />}
    </PageWrapper>
  );
};

export default CycleCount;
