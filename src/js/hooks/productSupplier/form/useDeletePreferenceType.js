import _ from 'lodash';

import confirmationModal from 'utils/confirmationModalUtils';

const useDeletePreferenceType = ({ preferenceTypeData, reset }) => {
  const isPreferenceTypeEmpty = !_.some(
    Object.values(_.omit(preferenceTypeData || {}, 'id')),
  );

  const defaultPreferenceType = {
    bidName: '',
    validityStartDate: '',
    validityEndDate: '',
    preferenceType: '',
  };

  const modalLabels = {
    title: {
      label: 'react.productSupplier.deleteConfirmation.title.label',
      default: 'Are you sure?',
    },
    content: {
      label: 'react.productSupplier.defaultPreferenceDeleteConfirmation.label',
      default: 'Are you sure you want to delete the default preference for this source?',
    },
  };

  const deleteConfirmationModalButtons = () => (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'react.productSupplier.deleteConfirmation.cancel.label',
      onClick: onClose,
    },
    {
      variant: 'danger',
      defaultLabel: 'Confirm',
      label: 'react.productSupplier.deleteConfirmation.delete.label',
      onClick: () => {
        reset({ defaultPreferenceType });
        onClose();
      },
    },
  ]);

  const openConfirmationModal = (productSupplierId) => {
    confirmationModal({
      buttons: deleteConfirmationModalButtons(productSupplierId),
      ...modalLabels,
    });
  };

  return {
    openConfirmationModal,
    isPreferenceTypeEmpty,
  };
};

export default useDeletePreferenceType;
