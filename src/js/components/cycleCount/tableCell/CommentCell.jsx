import React, { useState } from 'react';

import { useDispatch } from 'react-redux';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';

const CommentCell = ({
  initialValue,
  id,
  cycleCountId,
}) => {
  const [value, setValue] = useState(initialValue);

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
      />
    </TableCell>
  );
};

export default CommentCell;
