import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useAssignCountModal from 'hooks/cycleCount/useAssignCountModal';
import useTranslate from 'hooks/useTranslate';

const AssignCountModal = ({
  isOpen,
  closeModal,
  defaultTitleLabel,
  titleLabel,
  selectedCycleCountItems,
  setSelectedCycleCountItems,
  isCount,
  refetchData,
}) => {
  const translate = useTranslate();

  const { columns, handleAssign } = useAssignCountModal({
    selectedCycleCountItems,
    setSelectedCycleCountItems,
    isCount,
    refetchData,
    closeModal,
  });

  return (
    <Modal
      isOpen={isOpen}
      className="modal-content"
    >
      <div>
        <div className="d-flex justify-content-between align-items-center">
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
            data={selectedCycleCountItems}
            loading={false}
            disablePagination
            totalCount={selectedCycleCountItems.length}
            filterParams={{}}
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

export default AssignCountModal;

AssignCountModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  defaultTitleLabel: PropTypes.string.isRequired,
  titleLabel: PropTypes.string.isRequired,
  selectedCycleCountItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setSelectedCycleCountItems: PropTypes.func.isRequired,
  isCount: PropTypes.bool,
  refetchData: PropTypes.func,
};

AssignCountModal.defaultProps = {
  isCount: false,
  refetchData: null,
};
