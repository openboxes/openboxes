import React from 'react';

import CycleCountReportingHeader from 'components/cycleCountReporting/CycleCountReportingHeader';
import InventoryTransactionsTab from 'components/cycleCountReporting/InventoryTransactionsTab';
import ProductsTab from 'components/cycleCountReporting/ProductsTab';
import Tabs from 'components/listPagesUtils/Tabs';
import {
  INVENTORY_TRANSACTIONS_TAB,
  PRODUCTS_TAB,
} from 'consts/cycleCount';
import useCycleCountPagination from 'hooks/useCycleCountPagination';
import useQueryParams from 'hooks/useQueryParams';
import useSwitchTabs from 'hooks/useSwitchTabs';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CycleCountReporting = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: PRODUCTS_TAB });
  useTranslation('cycleCount');

  // After applying filters, add them as an argument to the hook below
  const tablePaginationProps = useCycleCountPagination({});

  const tabs = {
    [PRODUCTS_TAB]: {
      label: {
        id: 'react.cycleCount.products.label',
        defaultMessage: 'Products',
      },
      onClick: (tab) => switchTab(tab),
    },
    [INVENTORY_TRANSACTIONS_TAB]: {
      label: {
        id: 'react.cycleCount.inventoryTransactions.label',
        defaultMessage: 'Inventory Transactions',
      },
      onClick: (tab) => switchTab(tab),
    },
  };

  const { tab } = useQueryParams();

  return (
    <PageWrapper>
      <CycleCountReportingHeader />
      <div className="list-page-list-section">
        <Tabs config={tabs} className="m-3" />
        {tab === PRODUCTS_TAB && (
          <ProductsTab />
        )}

        {tab === INVENTORY_TRANSACTIONS_TAB && (
          // After applying filters, add them as an param to that component
          <InventoryTransactionsTab
            tablePaginationProps={tablePaginationProps}
            filterParams={{}}
          />
        )}
      </div>
    </PageWrapper>
  );
};

export default CycleCountReporting;
