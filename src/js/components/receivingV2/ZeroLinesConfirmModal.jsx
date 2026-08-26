import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';

import Button from 'components/form-elements/Button';
import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString, getDateFnsLocale } from 'utils/dateUtils';

import 'components/receivingV2/receiving.scss';
import 'utils/utils.scss';

/**
 * Confirmation shown before moving to the check step at a location without partial receiving,
 * listing the lines that are about to be received as zero.
 *
 * `confirmAlert` renders it outside the app tree, so there is no redux and no localize provider
 * here - the translation function and the locale are handed in by the caller.
 */
const ZeroLinesConfirmModal = ({
  lines, translate, localeKey, onConfirm, onCancel,
}) => (
  <div className="d-flex flex-column custom-modal-content justify-content-between bg-white">
    <div className="d-flex justify-content-between">
      <p className="custom-modal-title">
        {translate('react.receiving.zeroLines.confirm.title', 'Confirm receiving')}
      </p>
      <RiCloseFill
        size="32px"
        className="cursor-pointer"
        role="button"
        onClick={onCancel}
      />
    </div>
    <div>
      <p className="custom-modal-text">
        {translate(
          'react.receiving.zeroLines.confirm.message',
          `You have not entered a value in the receiving quantity for ${lines.length} line/lines on `
          + 'this shipment. These lines will be received as zero. You will not be able to go back '
          + 'and receive them later. Do you want to continue?',
          { count: lines.length },
        )}
      </p>
      <div className="zero-lines-table">
        <table>
          <thead>
            <tr>
              <th>{translate('react.receiving.code.label', 'Code')}</th>
              <th>{translate('react.receiving.product.label', 'Product')}</th>
              <th>{translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}</th>
              <th>{translate('react.receiving.expirationDate.short.label', 'Exp Date')}</th>
            </tr>
          </thead>
          <tbody>
            {lines.map((line) => (
              <tr key={line.rowId}>
                <td>{line.productCode}</td>
                <td>{line.product?.name}</td>
                <td>{line.lotNumber}</td>
                <td>
                  {formatDateToString({
                    date: line.expirationDate,
                    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
                    options: { locale: getDateFnsLocale(localeKey) },
                  })}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
    <div className="d-flex justify-content-end mt-4">
      <Button
        label="react.default.no.label"
        defaultLabel="No"
        variant="secondary"
        onClick={onCancel}
      />
      <Button
        label="react.default.yes.label"
        defaultLabel="Yes"
        variant="primary"
        onClick={onConfirm}
      />
    </div>
  </div>
);

ZeroLinesConfirmModal.propTypes = {
  // Rows left blank, listed in the order they appear in the receiving table.
  lines: PropTypes.arrayOf(PropTypes.shape({
    rowId: PropTypes.string,
    productCode: PropTypes.string,
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
  })).isRequired,
  translate: PropTypes.func.isRequired,
  // Locale code the expiration dates are formatted with.
  localeKey: PropTypes.string,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

ZeroLinesConfirmModal.defaultProps = {
  localeKey: undefined,
};

export default ZeroLinesConfirmModal;
