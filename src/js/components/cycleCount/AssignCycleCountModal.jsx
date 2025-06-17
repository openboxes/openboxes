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
  setSelectedCycleCounts,
  isRecount,
  refetchData,
}) => {
  const translate = useTranslate();

  const { columns, handleAssign } = useAssignCycleCountModal({
    selectedCycleCounts,
    setSelectedCycleCounts,
    isRecount,
    refetchData,
    closeModal,
  });

  return (
    isOpen ? (
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
              data={selectedCycleCounts}
              disablePagination
              totalCount={selectedCycleCounts.length}
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
    ) : null
  );
};

export default AssignCycleCountModal;

AssignCycleCountModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  defaultTitleLabel: PropTypes.string,
  titleLabel: PropTypes.string,
  selectedCycleCounts: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setSelectedCycleCounts: PropTypes.func.isRequired,
  isRecount: PropTypes.bool,
  refetchData: PropTypes.func,
};

AssignCycleCountModal.defaultProps = {
  titleLabel: 'react.cycleCount.modal.assignProductsToCount.title.label',
  defaultTitleLabel: 'Assign products to count',
  isRecount: false,
  refetchData: null,
};
