import React from 'react';

import PropTypes from 'prop-types';

import AssignCycleCountModal from 'components/cycleCount/AssignCycleCountModal';
import AddNewRecordFooter from 'components/cycleCount/tableFooter/AddNewRecordFooter';
import CountedByHeader from 'components/cycleCount/tableHeader/CountedByHeader';
import DateCountedHeader from 'components/cycleCount/tableHeader/DateCountedHeader';
import ProductDataHeader from 'components/cycleCount/tableHeader/ProductDataHeader';
import DataTable from 'components/DataTable/v2/DataTable';
import useCountStepTable from 'hooks/cycleCount/useCountStepTable';

import 'components/cycleCount/cycleCount.scss';

const CountStepTable = ({
  id,
  isStepEditable,
  isFormDisabled,
  isAssignCountModalOpen,
  closeAssignCountModal,
  assignCountModalData,
}) => {
  const {
    columns,
    cycleCountItems,
  } = useCountStepTable({
    cycleCountId: id,
    isStepEditable,
    isFormDisabled,
  });

  return (
    <>
      {isAssignCountModalOpen && (
      <AssignCycleCountModal
        isOpen={isAssignCountModalOpen}
        closeModal={closeAssignCountModal}
        selectedCycleCounts={assignCountModalData}
        defaultTitleLabel="Assign products to recount"
        titleLabel="react.cycleCount.modal.assignProductsToRecount.title.label"
        assignDataDirectly
        isRecount
        showSkipButton
      />
      )}
      <div className="list-page-list-section">
        <ProductDataHeader
          cycleCountId={id}
        />
        <div className="pt-3 pl-4 d-flex align-items-center">
          <DateCountedHeader
            isStepEditable={isStepEditable}
            isFormDisabled={isFormDisabled}
            cycleCountId={id}
          />
          <CountedByHeader
            isStepEditable={isStepEditable}
            isFormDisabled={isFormDisabled}
            cycleCountId={id}
          />
        </div>
        <div className="mx-4 count-step-table">
          <DataTable
            columns={columns}
            data={cycleCountItems}
            totalCount={cycleCountItems.length}
            filterParams={{}}
            disablePagination
          />
        </div>
        <AddNewRecordFooter
          cycleCountId={id}
          isStepEditable={isStepEditable}
          isFormDisabled={isFormDisabled}
        />
      </div>
    </>
  );
};

export default CountStepTable;

CountStepTable.propTypes = {
  id: PropTypes.string.isRequired,
  validationErrors: PropTypes.shape({}).isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  countedBy: PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  defaultCountedBy: PropTypes.shape({}).isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
  isAssignCountModalOpen: PropTypes.bool.isRequired,
  closeAssignCountModal: PropTypes.func.isRequired,
  assignCountModalData: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
};
