import React, { useState } from 'react';

import { useDispatch } from 'react-redux';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';

const CommentCell = ({
  initialValue,
  id,
  cycleCountId,
  isStepEditable,
}) => {
  const [value, setValue] = useState(initialValue);

  if (!isStepEditable) {
    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
      >
        {value}
      </TableCell>
    );
  }

  const dispatch = useDispatch();

  const onChange = (e) => {
    setValue(e.target.value);
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'comment',
        value,
      }),
    );
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <InputField
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className="m-1 w-75"
        hideErrorMessageWrapper
      />
    </TableCell>
  );
};

export default CommentCell;
