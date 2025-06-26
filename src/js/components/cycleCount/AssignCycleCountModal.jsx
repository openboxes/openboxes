import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useAssignCycleCountModal from 'hooks/cycleCount/useAssignCycleCountModal';
import useTranslate from 'hooks/useTranslate';

const AssignCycleCountModal = ({
  isOpen,
  closeModal,
  defaultTitleLabel,
  titleLabel,
  selectedCycleCounts,
  isRecount,
  refetchData,
  assignDataDirectly,
}) => {
  // When the modal is not displayed we want to show the scrollbar to users.
  // The return below is necessary to block executing hooks that are related to
  // the hidden modal.
  if (!isOpen) {
    document.body.style.overflowY = 'auto';
    return null;
  }

  const translate = useTranslate();

  const { columns, handleAssign } = useAssignCycleCountModal({
    selectedCycleCounts,
    isRecount,
    refetchData,
    closeModal,
    // Props used to assign count data directly to the cycle count
    // used in a case, when assigning data takes place after creating
    // the cycle count (for example, assigning after the discrepancy modal)
    assignDataDirectly,
  });

  return (
    <Modal
      isOpen={isOpen}
      className="modal-content"
    >
      <div>
        <div className="d-flex justify-content-between">
          <p className="assign-count-modal-header">
            {translate(titleLabel, defaultTitleLabel)}
          </p>
          <RiCloseFill
            size="32px"
            className="cursor-pointer"
            role="button"
            aria-label="Close modal"
            onClick={closeModal}
          />
        </div>
        <div className="assign-count-modal-container">
          <DataTable
            columns={columns}
            data={selectedCycleCounts.current}
            disablePagination
            totalCount={selectedCycleCounts.current?.length || 0}
          />
        </div>
        <div className="d-flex justify-content-end mt-3">
          <Button
            defaultLabel="Assign"
            label="react.cycleCount.assign.label"
            variant="primary"
            onClick={handleAssign}
          />
        </div>
      </div>
    </Modal>
  );
};

export default AssignCycleCountModal;

AssignCycleCountModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  defaultTitleLabel: PropTypes.string,
  titleLabel: PropTypes.string,
  selectedCycleCounts: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  isRecount: PropTypes.bool,
  refetchData: PropTypes.func,
  assignDataDirectly: PropTypes.bool,
};

AssignCycleCountModal.defaultProps = {
  titleLabel: 'react.cycleCount.modal.assignProductsToCount.title.label',
  defaultTitleLabel: 'Assign products to count',
  isRecount: false,
  refetchData: null,
  assignDataDirectly: false,
};
