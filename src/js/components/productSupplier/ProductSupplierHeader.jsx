import React from 'react';

import { useSelector } from 'react-redux';

import Button from 'components/form-elements/Button';
import ListTitle from 'components/listPagesUtils/ListTitle';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import RoleType from 'consts/roleType';
import { hasPermissions } from 'utils/permissionUtils';
import ListHeaderButtonsWrapper from 'wrappers/ListHeaderButtonsWrapper';
import ListHeaderWrapper from 'wrappers/ListHeaderWrapper';

const ProductSupplierHeader = () => {
  const {
    currentUser,
    isAdmin,
  } = useSelector((state) => ({
    currentUser: state.session.user,
    isAdmin: state.session.isUserAdmin,
  }));

  return (
    <ListHeaderWrapper>
      <ListTitle label={{
        id: 'react.productSupplier.header.label',
        defaultMessage: 'Product Sources List',
      }}
      />
      <ListHeaderButtonsWrapper>
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
      </ListHeaderButtonsWrapper>
    </ListHeaderWrapper>
  );
};

export default ProductSupplierHeader;
