import React from 'react';

import Button from 'components/form-elements/Button';
import ListTitle from 'components/listPagesUtils/ListTitle';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import ListHeaderButtonsWrapper from 'wrappers/ListHeaderButtonsWrapper';
import ListHeaderWrapper from 'wrappers/ListHeaderWrapper';

const ProductSupplierHeader = () => (
  <ListHeaderWrapper>
    <ListTitle label={{
      id: 'react.productSupplier.header.label',
      defaultMessage: 'Product Sources List',
    }}
    />
    <ListHeaderButtonsWrapper>
      <Button
        label="react.productSupplier.createProductSource.label"
        defaultLabel="Create Product Source"
        onClick={() => { window.location = PRODUCT_SUPPLIER_URL.create(); }}
      />
    </ListHeaderButtonsWrapper>
  </ListHeaderWrapper>
);

export default ProductSupplierHeader;
