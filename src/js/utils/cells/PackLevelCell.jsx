import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering up to two pack levels with a tooltip.
 */
const PackLevelCellComponent = ({
  packLevel1 = undefined, packLevel2 = undefined, label, defaultLabel,
}) => {
  const translate = useTranslate();

  return (
    <TableCell
      className="rt-td"
      customTooltip
      tooltipLabel={[packLevel1, packLevel2].filter(Boolean).join(' / ')}
    >
      <div className="pack-level" aria-label={translate(label, defaultLabel)}>
        {packLevel1 && <div className="pack-level__primary text-truncate">{packLevel1}</div>}
        {packLevel2 && <div className="pack-level__secondary text-truncate">{packLevel2}</div>}
      </div>
    </TableCell>
  );
};

const PackLevelCell = React.memo(PackLevelCellComponent);

PackLevelCell.displayName = 'PackLevelCell';

PackLevelCellComponent.propTypes = {
  packLevel1: PropTypes.string,
  packLevel2: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

export default PackLevelCell;
