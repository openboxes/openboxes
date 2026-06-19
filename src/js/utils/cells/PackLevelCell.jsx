import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering up to two pack levels with a tooltip.
 */
const PackLevelCell = React.memo(({
  packLevel1, packLevel2, label, defaultLabel,
}) => {
  const translate = useTranslate();

  return (
    <TableCell
      className="rt-td"
      customTooltip
      tooltipLabel={[packLevel1, packLevel2].filter(Boolean).join(' / ')}
    >
      <div aria-label={translate(label, defaultLabel)}>
        {packLevel1 && <div className="text-truncate">{packLevel1}</div>}
        {packLevel2 && <div className="text-truncate">{packLevel2}</div>}
      </div>
    </TableCell>
  );
});

PackLevelCell.displayName = 'PackLevelCell';

PackLevelCell.propTypes = {
  packLevel1: PropTypes.string,
  packLevel2: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

PackLevelCell.defaultProps = {
  packLevel1: undefined,
  packLevel2: undefined,
};

export default PackLevelCell;
