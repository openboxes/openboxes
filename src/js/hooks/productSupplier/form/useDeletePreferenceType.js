import _ from 'lodash';
import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import productSupplierApi from 'api/services/ProductSupplierApi';
import confirmationModal from 'utils/confirmationModalUtils';

const useDeletePreferenceType = ({
  preferenceTypeData,
  afterDelete,
  isDefaultPreferenceType,
  onCancel,
}) => {
  const dispatch = useDispatch();

  const isPreferenceTypeEmpty = !_.some(
    Object.values(_.omit(preferenceTypeData || {}, 'id')),
  );

  const modalLabels = {
    title: {
      label: 'react.productSupplier.deleteConfirmation.title.label',
      default: 'Are you sure?',
    },
    content: isDefaultPreferenceType
      ? {
        label: 'react.productSupplier.defaultPreferenceDeleteConfirmation.label',
        default: 'Are you sure you want to delete the default preference for this source?',
      }
      : {
        label: 'react.productSupplier.preferenceTypeVariationsDeleteConfirmation.label',
        default: 'Are you sure you want to delete this preference variation?',
      },
  };

  const deletePreferenceType = async (onClose) => {
    dispatch(showSpinner());
    try {
      if (preferenceTypeData?.id) {
        await productSupplierApi.deleteProductSupplierPreference(preferenceTypeData?.id);
      }
      afterDelete?.();
    } finally {
      onClose?.();
      dispatch(hideSpinner());
    }
  };

  const deleteConfirmationModalButtons = (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'react.productSupplier.deleteConfirmation.cancel.label',
      onClick: () => {
        onClose();
        onCancel?.();
      },
    },
    {
      variant: 'danger',
      defaultLabel: 'Confirm',
      label: 'react.productSupplier.deleteConfirmation.delete.label',
      onClick: () => deletePreferenceType(onClose),
    },
  ]);

  const openConfirmationModal = () => {
    if (preferenceTypeData?.id) {
      confirmationModal({
        buttons: deleteConfirmationModalButtons,
        handleOnClose: onCancel,
        ...modalLabels,
      });
      return;
    }

    deletePreferenceType();
  };

  return {
    openConfirmationModal,
    isPreferenceTypeEmpty,
  };
};

export default useDeletePreferenceType;

useDeletePreferenceType.propTypes = {
  preferenceTypeData: PropTypes.shape({}),
  afterDelete: PropTypes.func.isRequired,
  isDefaultPreferenceType: PropTypes.bool,
  onCancel: PropTypes.func,
};

useDeletePreferenceType.defaultProps = {
  preferenceTypeData: null,
  isDefaultPreferenceType: false,
  onCancel: () => {},
};
