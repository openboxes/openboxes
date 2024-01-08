import React, { useCallback, useState } from 'react';

import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import productSupplierApi from 'api/services/ProductSupplierApi';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';

const useProductSupplierActions = () => {
  const [isDeleteConfirmationOpened, setIsDeleteConfirmationOpened] = useState(false);
  const [selectedProductSupplierId, setSelectedProductSupplierId] = useState(null);

  const closeDeleteConfirmationModal = () => {
    setIsDeleteConfirmationOpened(false);
  };

  const deleteProductSupplier = async () => {
    try {
      await productSupplierApi.deleteProductSupplier(selectedProductSupplierId);
    } finally {
      setIsDeleteConfirmationOpened(false);
    }
  };

  const deleteConfirmationModalButtons = [
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'react.productSupplier.deleteConfirmation.cancel.label',
      onClick: closeDeleteConfirmationModal,
    },
    {
      variant: 'danger',
      defaultLabel: 'Delete',
      label: 'react.productSupplier.deleteConfirmation.delete.label',
      onClick: deleteProductSupplier,
    },
  ];

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
        setIsDeleteConfirmationOpened(true);
        setSelectedProductSupplierId(id);
      },
    },
  ], []);

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

  return {
    isDeleteConfirmationOpened,
    deleteConfirmationModalButtons,
    closeDeleteConfirmationModal,
    getActions,
    modalLabels,
  };
};

export default useProductSupplierActions;
