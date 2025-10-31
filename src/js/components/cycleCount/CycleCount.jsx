import React, { useLayoutEffect } from 'react';

import CycleCountAllProducts from 'components/cycleCount/allProductsTab/CycleCountAllProducts';
import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import CycleCountFilters from 'components/cycleCount/CycleCountFilters';
import CycleCountHeader from 'components/cycleCount/CycleCountHeader';
import CycleCountToCount from 'components/cycleCount/toCountTab/CycleCountToCount';
import CycleCountToResolve from 'components/cycleCount/toResolveTab/CycleCountToResolve';
import Tabs from 'components/listPagesUtils/Tabs';
import {
  ALL_PRODUCTS_TAB,
  TO_COUNT_TAB,
  TO_RESOLVE_TAB,
} from 'consts/cycleCount';
import useCycleCountFilters from 'hooks/cycleCount/useCycleCountFilters';
import useCycleCountPagination from 'hooks/useCycleCountPagination';
import useQueryParams from 'hooks/useQueryParams';
import useResetScrollbar from 'hooks/useResetScrollbar';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CycleCount = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: ALL_PRODUCTS_TAB });
  useTranslation('cycleCount');

  const { tab: currentTab } = useQueryParams();

  // Each tab will have different filters, that's why we will need this function
  const getFilterFields = () => {
    switch (currentTab) {
      case ALL_PRODUCTS_TAB:
        return cycleCountFilterFields.allProductsTab;
      case TO_COUNT_TAB:
        return cycleCountFilterFields.toCountTab;
      case TO_RESOLVE_TAB:
        return cycleCountFilterFields.toResolveTab;
      default:
        return cycleCountFilterFields.allProductsTab;
    }
  };
  const filterFields = getFilterFields();

  const {
    defaultFilterValues,
    setFilterValues,
    categories,
    internalLocations,
    tags,
    catalogs,
    abcClasses,
    countAssignees,
    countDeadline,
    recountAssignees,
    recountDeadline,
    negativeQuantity,
    filterParams,
    resetForm,
    isLoading,
    debouncedPeopleFetch,
  } = useCycleCountFilters({ filterFields });

  // This is needed to pass the selected checkboxes state from "All Products" to "To Count"
  const toCountTabCheckboxes = useTableCheckboxes();
  const { setCheckedCheckboxes } = toCountTabCheckboxes;

  // Moved this here to prevent resetting number of rows per page when switching tabs.
  const tablePaginationProps = useCycleCountPagination({ filterParams });
  const { pageSize, offset } = tablePaginationProps;

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
  };

  const { resetScrollbar } = useResetScrollbar({
    selector: 'body',
  });

  useLayoutEffect(() => {
    resetScrollbar();
  }, [currentTab, pageSize, offset]);

  return (
    <PageWrapper>
      <CycleCountHeader />
      <div className="list-page-list-section">
        <Tabs config={tabs} className="m-3" />
        <CycleCountFilters
          defaultValues={defaultFilterValues}
          setFilterParams={setFilterValues}
          filterFields={filterFields}
          formProps={{
            categories,
            catalogs,
            tags,
            internalLocations,
            abcClasses,
            countAssignees,
            countDeadline,
            recountAssignees,
            recountDeadline,
            negativeQuantity,
            debouncedPeopleFetch,
          }}
          isLoading={isLoading}
        />
        {currentTab === ALL_PRODUCTS_TAB && (
          <CycleCountAllProducts
            switchTab={switchTab}
            filterParams={filterParams}
            resetForm={resetForm}
            setToCountCheckedCheckboxes={setCheckedCheckboxes}
            tablePaginationProps={tablePaginationProps}
          />
        )}
        {currentTab === TO_COUNT_TAB && (
          <CycleCountToCount
            filterParams={filterParams}
            toCountTabCheckboxes={toCountTabCheckboxes}
            tablePaginationProps={tablePaginationProps}
          />
        )}
        {currentTab === TO_RESOLVE_TAB && (
          <CycleCountToResolve
            filterParams={filterParams}
            tablePaginationProps={tablePaginationProps}
          />
        )}
      </div>
    </PageWrapper>
  );
};

export default CycleCount;
