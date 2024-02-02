import React from 'react';

import useProductSupplierData from 'hooks/list-pages/productSupplier/useProductSupplierData';

import '../styles.scss';
import { useSelector } from 'react-redux';
import { translateWithDefaultMessage } from 'utils/Translate';
import { getTranslate } from 'react-localize-redux';

const CreateProductSupplierTitle = () => {
  const { productSupplier } = useProductSupplierData();

  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  return (
    <div className="create-page-title">
      <span className="create-page-tile-main-content">
        {productSupplier
          ? productSupplier?.name
          : translate('react.productSupplier.createProductSource.label', 'Create Product Source')}
      </span>
      {' '}
      {productSupplier && (
        <span className="create-page-title-source-code">
          (
          {productSupplier?.code}
          )
        </span>
      )}
    </div>
  );
};

export default CreateProductSupplierTitle;
