import React from 'react';

import { RiDeleteBinLine } from 'react-icons/ri';
import { Tooltip } from 'react-tippy';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

const ActionsCell = ({
  custom,
  id,
  isStepEditable,
  isFormDisabled,
  removeRow,
}) => {
  const translate = useTranslate();

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
