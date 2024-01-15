import React, { useCallback } from 'react';

import { confirmAlert } from 'react-confirm-alert';
import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import productSupplierApi from 'api/services/ProductSupplierApi';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import CustomConfirmModal from 'utils/CustomConfirmModal';
import { hasPermissionsToProductSourceActions } from 'utils/permissionUtils';

import 'react-confirm-alert/src/react-confirm-alert.css';

const useProductSupplierActions = ({ fireFetchData }) => {
  const { currentUser, isAdmin } = useSelector((state) => ({
    currentUser: state.session.user,
    isAdmin: state.session.isUserAdmin,
  }));

  const deleteProductSupplier = async (onClose, productSupplierId) => {
    try {
      await productSupplierApi.deleteProductSupplier(productSupplierId);
      fireFetchData?.();
    } finally {
      onClose?.();
    }
  };

  const modalLabels = {
    title: {
      label: 'react.productSupplier.deleteConfirmation.title.label',
      default: 'Are you sure?',
    },
    content: {
      label: 'react.productSupplier.deleteConfirmation.content.label',
      default: 'Are you sure you want to delete this Product Source?',
    },
  };

  const deleteConfirmationModalButtons = (onClose, productSupplierId) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'react.productSupplier.deleteConfirmation.cancel.label',
      onClick: onClose,
    },
    {
      variant: 'danger',
      defaultLabel: 'Delete',
      label: 'react.productSupplier.deleteConfirmation.delete.label',
      onClick: () => deleteProductSupplier(onClose, productSupplierId),
    },
  ]);

  const openConfirmationModal = (productSupplierId) => {
    confirmAlert({
      customUI: ({ onClose }) => (
        <CustomConfirmModal
          labels={modalLabels}
          onClose={onClose}
          buttons={deleteConfirmationModalButtons(onClose, productSupplierId)}
        />
      ),
    });
  };

  const getActions = useCallback((productSupplierId) => (hasPermissionsToProductSourceActions(currentUser, isAdmin) ? [
    {
      defaultLabel: 'Edit',
      label: 'react.productSupplier.edit.label',
      leftIcon: <RiPencilLine />,
      onClick: () => {
        window.location = PRODUCT_SUPPLIER_URL.edit(productSupplierId);
      },
    },
    {
      defaultLabel: 'Delete Product Source',
      label: 'react.productSupplier.delete.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: () => openConfirmationModal(productSupplierId),
    },
  ] : []), [currentUser, isAdmin]);

  return {
    getActions,
  };
};

export default useProductSupplierActions;
