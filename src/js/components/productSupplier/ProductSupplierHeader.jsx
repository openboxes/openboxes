import React from 'react';

import { useSelector } from 'react-redux';

import Button from 'components/form-elements/Button';
import ListTitle from 'components/listPagesUtils/ListTitle';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import RoleType from 'consts/roleType';
import { hasPermissions } from 'utils/permissionUtils';
import HeaderButtonsWrapper from 'wrappers/HeaderButtonsWrapper';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const ProductSupplierHeader = () => {
  const {
    currentUser,
    isAdmin,
  } = useSelector((state) => ({
    currentUser: state.session.user,
    isAdmin: state.session.isUserAdmin,
  }));

  return (
    <HeaderWrapper>
      <ListTitle label={{
        id: 'react.productSupplier.header.label',
        defaultMessage: 'Product Sources List',
      }}
      />
      <HeaderButtonsWrapper>
        {hasPermissions({
          user: currentUser,
          minimumRequiredRole: isAdmin,
          supplementalRoles: [RoleType.ROLE_PRODUCT_MANAGER],
        }) && (
          <Button
            label="react.productSupplier.createProductSource.label"
            defaultLabel="Create Product Source"
            onClick={() => {
              window.location = PRODUCT_SUPPLIER_URL.create();
            }}
          />
        )}
      </HeaderButtonsWrapper>
    </HeaderWrapper>
  );
};

export default ProductSupplierHeader;
