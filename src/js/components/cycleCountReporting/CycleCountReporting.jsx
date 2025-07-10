import React from 'react';

import cycleCountReportingFilterFields from 'components/cycleCountReporting/CycleCountReportingFilterFields';
import CycleCountReportingFilters from 'components/cycleCountReporting/CycleCountReportingFilters';
import CycleCountReportingHeader from 'components/cycleCountReporting/CycleCountReportingHeader';
import IndicatorsTab from 'components/cycleCountReporting/IndicatorsTab/IndicatorsTab';
import InventoryTransactionsTab from 'components/cycleCountReporting/InventoryTransactionsTab';
import ProductsTab from 'components/cycleCountReporting/ProductsTab';
import Tabs from 'components/listPagesUtils/Tabs';
import {
  INDICATORS_TAB,
  INVENTORY_TRANSACTIONS_TAB,
  PRODUCTS_TAB,
} from 'consts/cycleCount';
import useCycleCountReportingFilters from 'hooks/cycleCount/useCycleCountReportingFilters';
import useCycleCountPagination from 'hooks/useCycleCountPagination';
import useQueryParams from 'hooks/useQueryParams';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CycleCountReporting = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: PRODUCTS_TAB });
  useTranslation('cycleCount');
  const { tab: currentTab } = useQueryParams();

  // Each tab will have different filters, that's why we will need this function
  const getFilterFields = () => {
    switch (currentTab) {
      case PRODUCTS_TAB:
        return cycleCountReportingFilterFields.products;
      case INVENTORY_TRANSACTIONS_TAB:
        return cycleCountReportingFilterFields.inventoryTransactions;
      case INDICATORS_TAB:
        return cycleCountReportingFilterFields.indicators;
      default:
        return cycleCountReportingFilterFields.products;
    }
  };
  const filterFields = getFilterFields();
  const {
    defaultFilterValues,
    setFilterValues,
    isLoading,
    filterParams,
    resetForm,
    shouldFetch,
    setShouldFetch,
  } = useCycleCountReportingFilters({ filterFields });
  const tablePaginationProps = useCycleCountPagination(filterParams, setShouldFetch, null, false);
  const tabs = {
    [PRODUCTS_TAB]: {
      label: {
        id: 'react.cycleCount.products.label',
        defaultMessage: 'Products',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
    [INVENTORY_TRANSACTIONS_TAB]: {
      label: {
        id: 'react.cycleCount.inventoryTransactions.label',
        defaultMessage: 'Inventory Transactions',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
    [INDICATORS_TAB]: {
      label: {
        id: 'react.cycleCount.indicators.label',
        defaultMessage: 'Indicators',
      },
      onClick: (tab) => switchTab(tab, resetForm),
    },
  };

  return (
    <PageWrapper>
      <CycleCountReportingHeader />
      <div className="list-page-list-section">
        <Tabs config={tabs} className="m-3" />
        <CycleCountReportingFilters
          defaultValues={defaultFilterValues}
          setFilterParams={setFilterValues}
          filterFields={filterFields}
          isLoading={isLoading}
          setShouldFetch={setShouldFetch}
          tablePaginationProps={tablePaginationProps}
        />
        {currentTab === PRODUCTS_TAB && (
          <ProductsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={filterParams}
            shouldFetch={shouldFetch}
            setShouldFetch={setShouldFetch}
          />
        )}
        {currentTab === INVENTORY_TRANSACTIONS_TAB && (
          <InventoryTransactionsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={filterParams}
            shouldFetch={shouldFetch}
            setShouldFetch={setShouldFetch}
          />
        )}
        {currentTab === INDICATORS_TAB && (
          <IndicatorsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={filterParams}
          />
        )}
      </div>
    </PageWrapper>
  );
};

export default CycleCountReporting;
