import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import SummaryInfo from 'utils/SummaryInfo';

const EditLineItemModalFooter = ({ summaryData, onClose, isSaveDisabled }) => (
  <>
    <div className="mt-4">
      <SummaryInfo data={summaryData} />
    </div>
    <div className="d-flex justify-content-end align-items-center gap-8 mt-4">
      <Button
        label="react.default.button.cancel.label"
        defaultLabel="Cancel"
        variant="transparent"
        onClick={onClose}
      />
      <Button
        label="react.default.button.save.label"
        defaultLabel="Save"
        variant="primary"
        type="submit"
        disabled={isSaveDisabled}
      />
    </div>
  </>
);

EditLineItemModalFooter.propTypes = {
  summaryData: PropTypes.arrayOf(PropTypes.shape({
    title: PropTypes.string,
    data: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  })).isRequired,
  onClose: PropTypes.func.isRequired,
  isSaveDisabled: PropTypes.bool,
};

EditLineItemModalFooter.defaultProps = {
  isSaveDisabled: false,
};

export default EditLineItemModalFooter;
