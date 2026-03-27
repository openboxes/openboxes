import React from 'react';

// import Tippy from '@tippyjs/react';
import PropTypes from 'prop-types';
import { RiAddCircleLine } from 'react-icons/all';
import { useDispatch } from 'react-redux';

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
        {/* <Tippy */}
        {/*   className="d-flex align-items-center" */}
        {/*   content={( */}
        {/*     <span className="p-1"> */}
        {/*       {translate('react.cycleCount.addNewRecord.tooltip', 'Use this button to change lot number or bin location.')} */}
        {/*     </span> */}
        {/*   )} */}
        {/* > */}
          <Button
            onClick={handleAddEmptyRow}
            label="react.cycleCount.addNewRecord.label"
            defaultLabel="Add new record"
            variant="transparent"
            StartIcon={<RiAddCircleLine size={18} />}
            disabled={isFormDisabled}
          />
        {/* </Tippy> */}
      </div>
    )
  );
};

export default AddNewRecordFooter;

AddNewRecordFooter.propTypes = {
  cycleCountId: PropTypes.string.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
};
