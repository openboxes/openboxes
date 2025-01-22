import React from 'react';

import CycleCountAllProducts from 'components/cycleCount/allProductsTab/CycleCountAllProducts';
import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import CycleCountFilters from 'components/cycleCount/CycleCountFilters';
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
import useCycleCountFilters from 'hooks/cycleCount/useCycleCountFilters';
import useQueryParams from 'hooks/useQueryParams';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CycleCount = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: ALL_PRODUCTS_TAB });
  useTranslation('cycleCount');

  const {
    defaultFilterValues,
    setFilterValues,
    categories,
    internalLocations,
    tags,
    catalogs,
    abcClasses,
    negativeQuantity,
    filterParams,
    resetForm,
  } = useCycleCountFilters();

  const tabs = {
    [ALL_PRODUCTS_TAB]: {
      label: {
        id: 'react.cycleCount.allProducts.label',
        defaultMessage: 'All products',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
    [TO_COUNT_TAB]: {
      label: {
        id: 'react.cycleCount.toCount.label',
        defaultMessage: 'To count',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
    [TO_RESOLVE_TAB]: {
      label: {
        id: 'react.cycleCount.toResolve.label',
        defaultMessage: 'To resolve',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
    [TO_APPROVE_TAB]: {
      label: {
        id: 'react.cycleCount.toApprove.label',
        defaultMessage: 'To approve',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
  };

  const { tab } = useQueryParams();

  return (
    <PageWrapper>
      <CycleCountHeader />
      <div className="list-page-list-section">
        <Tabs config={tabs} className="m-3" />
        <CycleCountFilters
          defaultValues={defaultFilterValues}
          setFilterParams={setFilterValues}
          filterFields={cycleCountFilterFields}
          formProps={{
            categories,
            catalogs,
            tags,
            internalLocations,
            abcClasses,
            negativeQuantity,
          }}
        />
        {tab === ALL_PRODUCTS_TAB && (
        <CycleCountAllProducts
          switchTab={switchTab}
          filterParams={filterParams}
        />
        )}
        {tab === TO_COUNT_TAB && (
        <CycleCountToCount
          filterParams={filterParams}
        />
        )}
        {tab === TO_RESOLVE_TAB && <CycleCountToResolve />}
        {tab === TO_APPROVE_TAB && <CycleCountToApprove />}
      </div>
    </PageWrapper>
  );
};

export default CycleCount;
