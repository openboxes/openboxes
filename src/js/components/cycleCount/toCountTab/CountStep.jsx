import React, { useRef } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import CountStepHeader from 'components/cycleCount/toCountTab/CountStepHeader';
import CountStepTable from 'components/cycleCount/toCountTab/CountStepTable';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import useCountStep from 'hooks/cycleCount/useCountStep';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CountStep = () => {
  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
  }));
  const initialCurrentLocation = useRef(currentLocation?.id);
  const isFormDisabled = !_.isEqual(currentLocation?.id, initialCurrentLocation.current);
  const {
    tableData,
    printCountForm,
    next,
    save,
    resolveDiscrepancies,
    back,
    tableMeta,
    addEmptyRow,
    removeRow,
    assignCountedBy,
    getCountedDate,
    setCountedDate,
    validationErrors,
    isStepEditable,
    getCountedBy,
    getDefaultCountedBy,
    triggerValidation,
    refreshFocusCounter,
    isSaveDisabled,
    setIsSaveDisabled,
    validateExistenceOfCycleCounts,
    importItems,
    sortByProductName,
    setSortByProductName,
    importErrors,
    isAssignCountModalOpen,
    closeAssignCountModal,
    assignCountModalData,
    setAssignCountModalData,
  } = useCountStep();

  return (
    <PageWrapper>
      {isStepEditable ? (
        <CountStepHeader
          printCountForm={printCountForm}
          next={() => validateExistenceOfCycleCounts(next)}
          save={() => validateExistenceOfCycleCounts(save)}
          isFormDisabled={isFormDisabled}
          importItems={importItems}
          sortByProductName={sortByProductName}
          setSortByProductName={setSortByProductName}
          importErrors={importErrors}
        />
      ) : (
        <ConfirmStepHeader
          back={back}
          save={() => validateExistenceOfCycleCounts(resolveDiscrepancies)}
          isSaveDisabled={isSaveDisabled}
          setIsSaveDisabled={setIsSaveDisabled}
          isFormDisabled={isFormDisabled}
          redirectTab={TO_COUNT_TAB}
          redirectLabel="react.cycleCount.redirectToList.label"
          redirectDefaultMessage="Back to Cycle Count List"
        />
      )}
      {tableData
        .map(({ cycleCountItems, id }) => (
          <CountStepTable
            key={id}
            id={id}
            product={cycleCountItems[0]?.product}
            dateCounted={getCountedDate(id)}
            tableData={cycleCountItems}
            tableMeta={tableMeta}
            addEmptyRow={addEmptyRow}
            removeRow={removeRow}
            setCountedDate={setCountedDate(id)}
            assignCountedBy={assignCountedBy}
            validationErrors={validationErrors}
            isStepEditable={isStepEditable}
            countedBy={getCountedBy(id)}
            defaultCountedBy={getDefaultCountedBy(id)}
            triggerValidation={triggerValidation}
            refreshFocusCounter={refreshFocusCounter}
            isFormDisabled={isFormDisabled}
            isAssignCountModalOpen={isAssignCountModalOpen}
            closeAssignCountModal={closeAssignCountModal}
            assignCountModalData={assignCountModalData}
            setAssignCountModalData={setAssignCountModalData}
          />
        ))}
    </PageWrapper>
  );
};

export default CountStep;
