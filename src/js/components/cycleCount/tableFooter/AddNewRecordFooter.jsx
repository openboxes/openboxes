import React from 'react';

import { RiAddCircleLine } from 'react-icons/all';
import { useDispatch } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { addEmptyRow } from 'actions';
import Button from 'components/form-elements/Button';
import useTranslate from 'hooks/useTranslate';

const AddNewRecordFooter = ({ cycleCountId, isStepEditable, isFormDisabled }) => {
  const translate = useTranslate();

  const dispatch = useDispatch();

  const handleAddEmptyRow = () => {
    dispatch(addEmptyRow(cycleCountId));
  };

  return (
    isStepEditable && (
      <div
        className="ml-4 mb-3 d-flex"
      >
        <Tooltip
          className="d-flex align-items-center"
          html={(
            <span className="p-1">
              {translate('react.cycleCount.addNewRecord.tooltip', 'Use this button to change lot number or bin location.')}
            </span>
          )}
        >
          <Button
            onClick={handleAddEmptyRow}
            label="react.cycleCount.addNewRecord.label"
            defaultLabel="Add new record"
            variant="transparent"
            StartIcon={<RiAddCircleLine size={18} />}
            disabled={isFormDisabled}
          />
        </Tooltip>
      </div>
    )
  );
};

export default AddNewRecordFooter;
