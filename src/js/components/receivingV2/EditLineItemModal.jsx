import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';
import { useWatch } from 'react-hook-form';
import { RiAddCircleLine, RiArrowGoBackLine, RiCloseFill } from 'react-icons/ri';
import Modal from 'react-modal';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import Subsection from 'components/Layout/v2/Subsection';
import ShipmentItemDetails from 'components/receivingV2/ShipmentItemDetails';
import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivedLineItems from 'hooks/receiving/v2/useReceivedLineItems';
import useReceivingLineItems from 'hooks/receiving/v2/useReceivingLineItems';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import { formatDateToString } from 'utils/dateUtils';
import SummaryInfo from 'utils/SummaryInfo';

const EditLineItemModal = ({ onClose, lineItem }) => {
  const translate = useTranslate();
  const currentLocale = useSelector(getCurrentLocale);
  const {
    control, fields, columns, addRow, copyToReceiving, revertToOriginal,
  } = useReceivingLineItems(lineItem);
  const { receivedItems, columns: receivedColumns } = useReceivedLineItems(lineItem, {
    onCopyToReceive: copyToReceiving,
  });

  // Sum of the "Receiving now" inputs across the table, kept live as the user edits.
  const watchedLineItems = useWatch({ control, name: 'lineItems' });
  const receivingNow = (watchedLineItems ?? []).reduce(
    (sum, item) => sum + (Number(item?.quantityReceiving) || 0),
    0,
  );

  const quantityShipped = lineItem?.quantityShipped ?? 0;
  const received = lineItem?.quantityReceived ?? 0;
  const remainingToReceive = quantityShipped - received - receivingNow;

  // The original shipped values for the line being edited.
  // TODO: `status` and `location` are not yet exposed on the line item.
  const details = {
    status: 'SHIPPED',
    product: lineItem?.product?.name,
    lotNumber: lineItem?.lotNumber,
    // Formatted like the read-only table's expiration cell.
    expirationDate: formatDateToString({
      date: lineItem?.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
      options: { locale: locales[currentLocale] },
    }),
    recipient: lineItem?.recipient?.name,
    location: lineItem?.location?.name,
    quantityShipped: lineItem?.quantityShipped,
  };

  const summaryData = [
    {
      title: translate('react.receiving.quantityShipped.label', 'Quantity Shipped'),
      data: quantityShipped,
    },
    {
      title: translate('react.receiving.received.label', 'Received'),
      data: received,
    },
    {
      title: translate('react.receiving.receivingNow.label', 'Receiving Now'),
      data: receivingNow,
    },
    {
      title: translate('react.receiving.remainingToReceive.label', 'Remaining to Receive'),
      data: remainingToReceive,
    },
  ];

  return (
    <Modal isOpen className="modal-content">
      <div className="receiving-edit-modal" data-testid="receiving-edit-line-item-modal">
        <div className="d-flex justify-content-between align-items-center pb-2">
          <h5 className="receiving-edit-modal__title m-0 font-weight-500">
            {translate('react.receiving.editModal.title.label', 'Edit Receiving Information')}
          </h5>
          <RiCloseFill
            size="24px"
            className="cursor-pointer"
            role="button"
            aria-label="Close modal"
            onClick={onClose}
          />
        </div>
        <ShipmentItemDetails details={details} />
        <Section showTitle={false} className="receiving-edit-modal__received mt-4">
          <Subsection
            title={(
              <div className="badge-container">
                <Badge
                  label={translate('react.receiving.received.label', 'Received')}
                  variant="badge--green text-uppercase"
                />
              </div>
            )}
          >
            <DataTable
              columns={receivedColumns}
              data={receivedItems}
              totalCount={receivedItems.length}
              disablePagination
              emptyTableMessage={{
                id: 'react.receiving.emptyTable.label',
                defaultMessage: 'No items to receive',
              }}
            />
          </Subsection>
        </Section>
        <div className="d-flex justify-content-between align-items-center mt-4">
          <div className="badge-container">
            <Badge
              label={translate('react.receiving.receivingNow.label', 'Receiving Now')}
              variant="badge--primary"
            />
          </div>
          <Button
            label="react.receiving.revertToOriginal.label"
            defaultLabel="Revert to original"
            variant="secondary"
            EndIcon={<RiArrowGoBackLine size={18} />}
            onClick={revertToOriginal}
          />
        </div>
        <form className="mt-2">
          <DataTable
            columns={columns}
            data={fields}
            totalCount={fields.length}
            disablePagination
            showFooter
            meta={{ totalReceivingNow: receivingNow }}
            emptyTableMessage={{
              id: 'react.receiving.emptyTable.label',
              defaultMessage: 'No items to receive',
            }}
          />
        </form>
        <button
          type="button"
          className="receiving-edit-modal__add-record d-flex align-items-center gap-8 p-0 border-0 bg-transparent cursor-pointer font-weight-500"
          onClick={addRow}
        >
          <RiAddCircleLine size={18} />
          {translate('react.receiving.addNewRecord.label', 'Add new record')}
        </button>
        <div className="mt-4">
          <SummaryInfo data={summaryData} />
        </div>
        <div className="d-flex justify-content-end align-items-center gap-8 mt-4">
          {/* TODO: no-op for now. Wired up in a later iteration. */}
          <Button
            label="react.default.button.cancel.label"
            defaultLabel="Cancel"
            variant="transparent"
            onClick={onClose}
          />
          <Button
            label="react.default.button.save.label"
            defaultLabel="Save"
            variant="primary"
          />
        </div>
      </div>
    </Modal>
  );
};

EditLineItemModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  lineItem: PropTypes.shape({
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
    recipient: PropTypes.shape({ name: PropTypes.string }),
    location: PropTypes.shape({ name: PropTypes.string }),
    quantityShipped: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    quantityReceived: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  }),
};

EditLineItemModal.defaultProps = {
  lineItem: undefined,
};

export default EditLineItemModal;
