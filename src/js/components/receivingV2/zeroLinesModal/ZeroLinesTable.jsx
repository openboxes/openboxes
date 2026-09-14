import React from 'react';

import PropTypes from 'prop-types';

import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString, getDateFnsLocale } from 'utils/dateUtils';

import 'components/receivingV2/receiving.scss';

/**
 * The lines about to be received as zero, so the user can tell which ones they are without
 * leaving the modal. Scrolls once the shipment has more of them than the modal can show.
 */
const ZeroLinesTable = ({ lines, translate, localeKey }) => (
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
);

ZeroLinesTable.propTypes = {
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
};

ZeroLinesTable.defaultProps = {
  localeKey: undefined,
};

export default ZeroLinesTable;
