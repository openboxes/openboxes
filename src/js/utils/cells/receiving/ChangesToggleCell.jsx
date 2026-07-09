import React from 'react';

import PropTypes from 'prop-types';
import { FaChevronDown, FaChevronUp } from 'react-icons/fa';

import { TableCell } from 'components/DataTable';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';

/**
 * The cell of a toggle row - a chevron + label button expanding/collapsing
 * the split items of the row.
 */
const ChangesToggleCell = React.memo(({ isExpanded, onToggle, changeCount }) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();

  const changeCountFormatted = formatNumber(changeCount);

  return (
    <TableCell className={`rt-td receiving-table__changes-toggle ${isExpanded ? 'receiving-table__changes-toggle--expanded' : ''}`}>
      <button
        type="button"
        className="receiving-table__expand-button d-inline-flex align-items-center p-0 border-0 bg-transparent"
        onClick={onToggle}
      >
        {isExpanded
          ? <FaChevronUp size={12} />
          : <FaChevronDown size={12} />}
        <span className="receiving-table__changes-label">
          {translate('react.receiving.changesApplied.label', `${changeCountFormatted} changes applied`, [changeCountFormatted])}
        </span>
      </button>
    </TableCell>
  );
});

ChangesToggleCell.displayName = 'ChangesToggleCell';

ChangesToggleCell.propTypes = {
  isExpanded: PropTypes.bool,
  onToggle: PropTypes.func.isRequired,
  changeCount: PropTypes.number,
};

ChangesToggleCell.defaultProps = {
  isExpanded: false,
  changeCount: null,
};

export default ChangesToggleCell;
