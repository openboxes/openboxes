import React, { useMemo } from 'react';

import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';
import { makeGetCycleCountItem } from 'selectors';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

const ActionsCell = ({
  id,
  cycleCountId,
  isStepEditable,
  isFormDisabled,
  removeRow,
}) => {
  const translate = useTranslate();

  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );
  const { custom } = item;

  return (
    <TableCell className="rt-td d-flex justify-content-center count-step-actions">
      <Tooltip
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
        className="text-overflow-ellipsis"
        html={(
          <span className="p-2">
            {translate('react.default.button.delete.label', 'Delete')}
          </span>
        )}
        disabled={id}
      >
        {(id?.includes('newRow') || custom) && isStepEditable && (
          <RiDeleteBinLine
            className={isFormDisabled ? 'disabled-icon' : 'cursor-pointer'}
            onClick={() => removeRow(id)}
            size={22}
          />
        )}
      </Tooltip>
    </TableCell>
  );
};

export default ActionsCell;
