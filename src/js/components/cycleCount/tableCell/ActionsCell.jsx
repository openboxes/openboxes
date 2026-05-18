import React, { useMemo } from 'react';

// import Tippy from '@tippyjs/react';
import PropTypes from 'prop-types';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { makeGetCycleCountItem } from 'selectors';

import { TableCell } from 'components/DataTable';
import { NEW_ROW } from 'consts/cycleCount';
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
      {/* <Tippy */}
      {/*   arrow="true" */}
      {/*   delay="150" */}
      {/*   duration="250" */}
      {/*   hideDelay="50" */}
      {/*   className="text-overflow-ellipsis" */}
      {/*   content={( */}
      {/*     <span className="p-2"> */}
      {/*       {translate('react.default.button.delete.label', 'Delete')} */}
      {/*     </span> */}
      {/*   )} */}
      {/*   disabled={id} */}
      {/* > */}
        {(id?.includes(NEW_ROW) || custom) && isStepEditable && (
          <RiDeleteBinLine
            className={isFormDisabled ? 'disabled-icon' : 'cursor-pointer'}
            onClick={() => removeRow(id)}
            size={22}
          />
        )}
      {/* </Tippy> */}
    </TableCell>
  );
};

export default ActionsCell;

ActionsCell.propTypes = {
  id: PropTypes.string.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
  removeRow: PropTypes.func.isRequired,
};
