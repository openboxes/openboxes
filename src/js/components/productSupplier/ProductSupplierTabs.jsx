import React from 'react';

import { DETAILS_TAB, PREFERENCE_TYPES_TAB } from 'consts/productSupplierList';
import useProductSupplierTabs from 'hooks/list-pages/productSupplier/useProductSupplierTabs';
import useQueryParams from 'hooks/useQueryParams';
import Translate from 'utils/Translate';

const tabs = {
  [DETAILS_TAB]: {
    label: {
      id: 'react.productSupplier.tabs.details.label',
      defaultMessage: 'Details',
    },
  },
  [PREFERENCE_TYPES_TAB]: {
    label: {
      id: 'react.productSupplier.tabs.preferenceTypes.label',
      defaultMessage: 'Preference Types',
    },
  },
};

const ProductSupplierTabs = () => {
  const parsedQueryParams = useQueryParams();
  const { switchTab } = useProductSupplierTabs();

  return (
    <div className="tabs d-flex align-items-center">
      {Object.entries(tabs).map(([key, value]) => (
        <span
          key={key}
          className={parsedQueryParams?.tab === key ? 'active-tab' : ''}
          onClick={() => switchTab(key)}
          role="presentation"
        >
          <Translate id={value.label.id} defaultMessage={value.label.defaultMessage} />
        </span>
      ))}
    </div>
  );
};

export default ProductSupplierTabs;
