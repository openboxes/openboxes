import React from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import ModalWithTable from 'components/modals/ModalWithTable';
import useConfirmExpiryChangeWithDepotsModal from 'hooks/useConfirmExpiryChangeWithDepotsModal';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';

/**
 * Confirms expiration dates about to be saved on lots that are in stock, naming the depots each
 * change will reach.
 */
const ConfirmExpiryChangeWithDepotsModal = ({
  isOpen, data, onConfirm, onCancel,
}) => {
  useTranslation('confirmExpirationDate');
  const translate = useTranslate();
  const { columns, formatDate, formatLotNumber } = useConfirmExpiryChangeWithDepotsModal();
  const confirmationWarning = data.length > 1
    ? translate(
      'react.confirmExpirationDate.modal.multipleLotsWarning.label',
      'Updating the expiration date for these lots will update it for every depot in the system. If you have verified that the new expiration dates are correct, press OK to move forward.',
    )
    : translate(
      'react.confirmExpirationDate.modal.singleLotWarning.label',
      'Updating the expiration date for this lot will update it for every depot in the system. If you have verified that the new expiration date is correct, press OK to move forward.',
    );

  return (
    <ModalWithTable
      isOpen={isOpen}
      title={translate('react.confirmExpirationDate.modal.title.label', 'Confirm save')}
      cancelLabel={{ id: 'react.default.button.cancel.label', defaultMessage: 'Cancel' }}
      confirmLabel={{ id: 'react.default.ok.label', defaultMessage: 'Ok' }}
      onCancel={onCancel}
      onConfirm={onConfirm}
    >
      <div className="expiry-change-modal" data-testid="confirm-expiry-change-with-depots-modal">
        {data.map((item) => (
          <div className="expiry-change" key={`${item.product?.id}-${item.lotNumber}-${item.newExpiry}`}>
            <p className="expiry-change__product">{`${item.code} ${item.product?.name}`}</p>
            <p className="expiry-change__title">
              {translate(
                'react.confirmExpirationDate.modal.lotChanged.label',
                // eslint-disable-next-line no-template-curly-in-string
                'You have changed the expiration date for lot ${0} from ${1} to ${2}. This lot is in inventory in the following locations:',
                [
                  <strong key="lotNumber">{formatLotNumber(item.lotNumber)}</strong>,
                  <strong key="previousExpiry">{formatDate(item.previousExpiry)}</strong>,
                  <strong key="newExpiry">{formatDate(item.newExpiry)}</strong>,
                ],
              )}
            </p>
            <DataTable
              totalCount={item.depots.length}
              data={item.depots}
              columns={columns}
              disablePagination
            />
          </div>
        ))}
        <p className="expiry-change-modal__warning">{confirmationWarning}</p>
      </div>
    </ModalWithTable>
  );
};

ConfirmExpiryChangeWithDepotsModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  data: PropTypes.arrayOf(PropTypes.shape({
    code: PropTypes.string,
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    previousExpiry: PropTypes.string,
    newExpiry: PropTypes.string,
    depots: PropTypes.arrayOf(PropTypes.shape({
      depot: PropTypes.shape({ name: PropTypes.string }),
      quantityOnHand: PropTypes.number,
    })).isRequired,
  })),
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

ConfirmExpiryChangeWithDepotsModal.defaultProps = {
  data: [],
};

export default ConfirmExpiryChangeWithDepotsModal;
