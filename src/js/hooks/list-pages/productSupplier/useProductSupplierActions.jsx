import React, { useCallback, useState } from 'react';

import { confirmAlert } from 'react-confirm-alert';
import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import productSupplierApi from 'api/services/ProductSupplierApi';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import CustomConfirmModal from 'utils/CustomConfirmModal';

import 'react-confirm-alert/src/react-confirm-alert.css';

const useProductSupplierActions = () => {
  const [selectedProductSupplierId, setSelectedProductSupplierId] = useState(null);

  const deleteProductSupplier = async (onClose) => {
    try {
      await productSupplierApi.deleteProductSupplier(selectedProductSupplierId);
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

  const deleteConfirmationModalButtons = (onClose) => ([
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
      onClick: () => deleteProductSupplier(onClose),
    },
  ]);

  const openConfirmationModal = () => {
    confirmAlert({
      customUI: ({ onClose }) => (
        <CustomConfirmModal
          labels={modalLabels}
          onClose={onClose}
          buttons={deleteConfirmationModalButtons(onClose)}
        />
      ),
      buttons: deleteConfirmationModalButtons,
    });
  };

  const getActions = useCallback((id) => [
    {
      defaultLabel: 'Edit',
      label: 'react.productSupplier.edit.label',
      leftIcon: <RiPencilLine />,
      onClick: () => {
        window.location = PRODUCT_SUPPLIER_URL.edit(id);
      },
    },
    {
      defaultLabel: 'Delete Product Source',
      label: 'react.productSupplier.delete.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: () => {
        setSelectedProductSupplierId(id);
        openConfirmationModal();
      },
    },
  ], []);

  return {
    getActions,
  };
};

export default useProductSupplierActions;
