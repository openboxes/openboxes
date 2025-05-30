import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';
import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import cycleCountApi from 'api/services/CycleCountApi';
import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import { DateFormat } from 'consts/timeFormat';
import useAssignModalTable from 'hooks/cycleCount/useAssignModalTable';
import useSpinner from 'hooks/useSpinner';
import dateWithoutTimeZone from 'utils/dateUtils';
import Translate from 'utils/Translate';

const AssignCountModal = ({
  isOpen,
  closeModal,
  defaultTitleLabel,
  titleLabel,
  selectedCycleCountItems,
  onUpdate,
  isCount,
  refetchData,
}) => {
  const spinner = useSpinner();
  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
  }));
  const { columns } = useAssignModalTable({ onUpdate });

  const handleAssign = async () => {
    try {
      spinner.show();

      const requests = selectedCycleCountItems.map((item) => {
        const { cycleCountRequestId, assignee, deadline } = item;

        const payload = isCount
          ? {
            requestedCountBy: assignee?.id,
            requestedCountDate: dateWithoutTimeZone({
              date: deadline,
              outputDateFormat: DateFormat.MM_DD_YYYY,
            }),
          }
          : {
            requestedRecountBy: assignee?.id,
            requestedRecountDate: dateWithoutTimeZone({
              date: deadline,
              outputDateFormat: DateFormat.MM_DD_YYYY,
            }),
          };

        return cycleCountApi.updateCycleCountRequest(
          currentLocation?.id,
          cycleCountRequestId,
          payload,
        );
      });

      await Promise.all(requests);
      if (refetchData) {
        refetchData();
      }
    } finally {
      closeModal();
      spinner.hide();
    }
  };

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflowY = 'hidden';
    }
    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, [isOpen]);

  return (
    <Modal
      isOpen={isOpen}
      className="modal-content"
    >
      <div>
        <div className="d-flex justify-content-between align-items-center">
          <p className="assign-count-modal-header">
            <Translate id={titleLabel} defaultMessage={defaultTitleLabel} />
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
  selectedCycleCountItems: PropTypes.arrayOf(
    PropTypes.shape({
      cycleCountRequestId: PropTypes.string.isRequired,
      product: PropTypes.shape({
        id: PropTypes.string.isRequired,
        productCode: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
      }).isRequired,
      assignee: PropTypes.string.isRequired,
      deadline: PropTypes.string.isRequired,
      inventoryItems: PropTypes.number.isRequired,
    }),
  ).isRequired,
  onUpdate: PropTypes.func.isRequired,
  isCount: PropTypes.bool,
  refetchData: PropTypes.func,
};

AssignCountModal.defaultProps = {
  isCount: false,
  refetchData: null,
};
