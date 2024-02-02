import React from 'react';

import { useHistory } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import CreateProductSupplierTitle from 'components/productSupplier/create/CreateProductSupplierTitle';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import useProductSupplierActions from 'hooks/list-pages/productSupplier/useProductSupplierActions';
import useProductSupplierData from 'hooks/list-pages/productSupplier/useProductSupplierData';
import RedirectButton from 'utils/RedirectButton';
import HeaderButtonsWrapper from 'wrappers/HeaderButtonsWrapper';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const CreateProductSupplierHeader = () => {
  const history = useHistory();
  const { productSupplier } = useProductSupplierData();

  const redirectToListPage = () => {
    history.push(PRODUCT_SUPPLIER_URL.list());
  };

  const { openConfirmationModal } = useProductSupplierActions({
    fireFetchData: redirectToListPage,
  });

  return (
    <HeaderWrapper className="h-auto">
      <div className="w-100 d-flex justify-content-around flex-column">
        <div className="product-supplier-buttons-container d-flex justify-content-between w-100">
          <RedirectButton
            label="react.productSupplier.redirectToList.label"
            defaultMessage="Back to Product Source List"
            redirectTo={PRODUCT_SUPPLIER_URL.list()}
          />
          <HeaderButtonsWrapper>
            {productSupplier && (
              <Button
                label="react.productSupplier.delete.label"
                defaultLabel="Delete Product Source"
                variant="danger-outline"
                onClick={() => openConfirmationModal(productSupplier?.id)}
              />
            )}
            <Button
              label="react.productSupplier.deleteConfirmation.cancel.label"
              defaultLabel="Cancel"
              variant="primary-outline"
              onClick={redirectToListPage}
            />
            <Button
              label="react.productSupplier.save.label"
              defaultLabel="Save"
              variant="primary"
              disabled
            />
          </HeaderButtonsWrapper>
        </div>
        <CreateProductSupplierTitle />
      </div>
    </HeaderWrapper>
  );
};

export default CreateProductSupplierHeader;
