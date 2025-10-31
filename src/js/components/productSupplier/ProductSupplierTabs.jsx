import React from 'react';

import Tabs from 'components/listPagesUtils/Tabs';
import { DETAILS_TAB, PREFERENCE_TYPES_TAB } from 'consts/productSupplierList';
import useSwitchTabs from 'hooks/useSwitchTabs';

const ProductSupplierTabs = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: DETAILS_TAB });

  const tabs = {
    [DETAILS_TAB]: {
      label: {
        id: 'react.productSupplier.tabs.details.label',
        defaultMessage: 'Details',
      },
      onClick: (key) => switchTab(key),
    },
    [PREFERENCE_TYPES_TAB]: {
      label: {
        id: 'react.productSupplier.tabs.preferenceTypes.label',
        defaultMessage: 'Preference Types',
      },
      onClick: (key) => switchTab(key),
    },
  };

  return (
    <Tabs config={tabs} />
  );
};

export default ProductSupplierTabs;
