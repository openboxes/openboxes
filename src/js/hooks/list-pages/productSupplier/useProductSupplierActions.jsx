import React, { useCallback } from 'react';

import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import productSupplierApi from 'api/services/ProductSupplierApi';
import { PRODUCT_SUPPLIER_EXPORT } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import NotificationType from 'consts/notificationTypes';
import RoleType from 'consts/roleType';
import useSpinner from 'hooks/useSpinner';
import useUserHasPermissions from 'hooks/useUserHasPermissions';
import confirmationModal from 'utils/confirmationModalUtils';
import exportFileFromAPI from 'utils/file-download-util';
import translate from 'utils/Translate';

const useProductSupplierActions = ({ fireFetchData, filterParams }) => {
  const spinner = useSpinner();
  const canManageProducts = useUserHasPermissions({
    minRequiredRole: RoleType.ROLE_ADMIN,
    supplementalRoles: [RoleType.ROLE_PRODUCT_MANAGER],
  });

  const deleteProductSupplier = async (onClose, productSupplierId) => {
    try {
      await productSupplierApi.deleteProductSupplier(productSupplierId);
      notification(NotificationType.SUCCESS)({
        message: translate({
          id: 'react.productSupplier.deleted.label',
          defaultMessage: `Product Source ${productSupplierId} deleted`,
          data: {
            id: productSupplierId,
          },
        }),
      });
      fireFetchData?.();
    } finally {
      onClose?.();
    }
  };

  const exportProductSuppliers = (exportResults = false) => {
    spinner.show();
    let params = {
      includeInactive: true,
      disableMaxLimit: true,
      format: 'xls',
    };

    if (exportResults) {
      params = {
        ...params,
        ...filterParams,
        product: filterParams.product?.id,
        supplier: filterParams.supplier?.id,
        defaultPreferenceTypes: (filterParams?.defaultPreferenceTypes || []).map(({ id }) => id),
      };
    }

    exportFileFromAPI({
      url: PRODUCT_SUPPLIER_EXPORT,
      params,
      format: 'xls',
      afterExporting: () => spinner.hide(),
    });
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

  const deleteConfirmationModalButtons = (productSupplierId) => (onClose) => ([
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
    confirmationModal({
      buttons: deleteConfirmationModalButtons(productSupplierId),
      ...modalLabels,
    });
  };

  const getActions = useCallback((productSupplierId) => (canManageProducts
    ? [
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
    ] : []), [canManageProducts]);

  return {
    getActions,
    exportProductSuppliers,
    openConfirmationModal,
  };
};

export default useProductSupplierActions;
