import React from 'react';

import PropTypes from 'prop-types';

import ZeroLinesConfirmModalFooter from 'components/receivingV2/zeroLinesModal/ZeroLinesConfirmModalFooter';
import ZeroLinesConfirmModalHeader from 'components/receivingV2/zeroLinesModal/ZeroLinesConfirmModalHeader';
import ZeroLinesTable from 'components/receivingV2/zeroLinesModal/ZeroLinesTable';

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
    <ZeroLinesConfirmModalHeader
      title={translate('react.receiving.zeroLines.confirm.title', 'Confirm receiving')}
      onClose={onCancel}
    />
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
      <ZeroLinesTable lines={lines} translate={translate} localeKey={localeKey} />
    </div>
    <ZeroLinesConfirmModalFooter onConfirm={onConfirm} onCancel={onCancel} />
  </div>
);

ZeroLinesConfirmModal.propTypes = {
  // Rows left blank, listed in the order they appear in the receiving table.
  lines: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
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
