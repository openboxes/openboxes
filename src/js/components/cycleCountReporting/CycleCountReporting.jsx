import React, { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getCurrentLocationSupportedActivities } from 'selectors';

import { fetchReasonCodes } from 'actions';
import { FETCH_CYCLE_COUNT_REASON_CODES } from 'actions/types';
import AutoIssuanceTransactionsTab from 'components/cycleCountReporting/AutoIssuanceTransactionsTab';
import cycleCountReportingFilterFields from 'components/cycleCountReporting/CycleCountReportingFilterFields';
import CycleCountReportingFilters from 'components/cycleCountReporting/CycleCountReportingFilters';
import CycleCountReportingHeader from 'components/cycleCountReporting/CycleCountReportingHeader';
import IndicatorsTab from 'components/cycleCountReporting/IndicatorsTab/IndicatorsTab';
import InventoryTransactionsTab from 'components/cycleCountReporting/InventoryTransactionsTab';
import ProductsTab from 'components/cycleCountReporting/ProductsTab';
import Tabs from 'components/listPagesUtils/Tabs';
import ActivityCode from 'consts/activityCode';
import {
  AUTO_ISSUANCE_TAB,
  INDICATORS_TAB,
  INVENTORY_TRANSACTIONS_TAB,
  PRODUCTS_TAB,
} from 'consts/cycleCount';
import useCycleCountReportingFilters from 'hooks/cycleCount/useCycleCountReportingFilters';
import useCycleCountPagination from 'hooks/useCycleCountPagination';
import useQueryParams from 'hooks/useQueryParams';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTranslation from 'hooks/useTranslation';
import { supports } from 'utils/supportedActivitiesUtils';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CycleCountReporting = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: PRODUCTS_TAB });
  useTranslation('cycleCount');
  const { tab: currentTab } = useQueryParams();
  const dispatch = useDispatch();

  // Auto-Issuance Transactions is only relevant to facilities that allow negative inventory, so
  // it stays hidden everywhere else instead of showing an empty/irrelevant report by default.
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const showAutoIssuanceTab = supports(supportedActivities, ActivityCode.ALLOW_NEGATIVE_INVENTORY);

  // Each tab will have different filters, that's why we will need this function
  const getFilterFields = () => {
    switch (currentTab) {
      case PRODUCTS_TAB:
        return cycleCountReportingFilterFields.products;
      case INVENTORY_TRANSACTIONS_TAB:
        return cycleCountReportingFilterFields.inventoryTransactions;
      case AUTO_ISSUANCE_TAB:
        return showAutoIssuanceTab
          ? cycleCountReportingFilterFields.autoIssuanceTransactions
          : cycleCountReportingFilterFields.products;
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
    updateParams,
    isLoading,
    filterParams,
    shouldFetch,
    setShouldFetch,
    filtersInitialized,
  } = useCycleCountReportingFilters({ filterFields });
  const tablePaginationProps = useCycleCountPagination({
    filterParams,
    setShouldFetch,
    defaultPageSize: currentTab === PRODUCTS_TAB ? 10 : 25,
  });
  const tabs = {
    [PRODUCTS_TAB]: {
      label: {
        id: 'react.cycleCount.products.label',
        defaultMessage: 'Products',
      },
      onClick: (tab) => switchTab(tab, () => updateParams({ isTabSwitch: true })),
    },
    [INVENTORY_TRANSACTIONS_TAB]: {
      label: {
        id: 'react.cycleCount.inventoryTransactions.label',
        defaultMessage: 'Inventory Transactions',
      },
      onClick: (tab) => switchTab(tab, () => updateParams({ isTabSwitch: true })),
    },
    ...(showAutoIssuanceTab ? {
      [AUTO_ISSUANCE_TAB]: {
        label: {
          id: 'react.cycleCount.autoIssuanceTransactions.label',
          defaultMessage: 'Auto-Issuance Transactions',
        },
        onClick: (tab) => switchTab(tab, () => updateParams({ isTabSwitch: true })),
      },
    } : {}),
    [INDICATORS_TAB]: {
      label: {
        id: 'react.cycleCount.indicators.label',
        defaultMessage: 'Indicators',
      },
      onClick: (tab) => switchTab(tab, () => updateParams({ isTabSwitch: true })),
    },
  };

  // Fetch reason codes at this level, because we should avoid fetching while switching tabs, and we
  // should fetch fresh cycle counts reason codes after refreshing the page (due to redux persist
  // we can't just check the presence in the store),
  useEffect(() => {
    dispatch(fetchReasonCodes('CYCLE_COUNT', FETCH_CYCLE_COUNT_REASON_CODES));
  }, []);

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
            filtersInitialized={filtersInitialized}
          />
        )}
        {currentTab === INVENTORY_TRANSACTIONS_TAB && (
          <InventoryTransactionsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={filterParams}
            shouldFetch={shouldFetch}
            setShouldFetch={setShouldFetch}
            filtersInitialized={filtersInitialized}
          />
        )}
        {currentTab === AUTO_ISSUANCE_TAB && showAutoIssuanceTab && (
          <AutoIssuanceTransactionsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={filterParams}
            shouldFetch={shouldFetch}
            setShouldFetch={setShouldFetch}
            filtersInitialized={filtersInitialized}
          />
        )}
        {currentTab === INDICATORS_TAB && (
          <IndicatorsTab
            filterParams={filterParams}
            shouldFetch={shouldFetch}
            setShouldFetch={setShouldFetch}
            filtersInitialized={filtersInitialized}
          />
        )}
      </div>
    </PageWrapper>
  );
};

export default CycleCountReporting;
