import React, { useMemo, useRef } from 'react';

import _ from 'lodash';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import CountStepHeader from 'components/cycleCount/toCountTab/CountStepHeader';
import VirtualizedTablesList from 'components/cycleCount/toCountTab/VirtualizedTablesList';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import useCountStep from 'hooks/cycleCount/useCountStep';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CountStep = () => {
  const {
    currentLocationId,
    cycleCountIds,
    printCountForm,
    next,
    resolveDiscrepancies,
    back,
    isStepEditable,
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
    handleCountStepHeaderSave,
  } = useCountStep();

  const initialCurrentLocation = useRef(currentLocationId);
  const isFormDisabled = useMemo(() =>
    !_.isEqual(currentLocationId, initialCurrentLocation.current), [currentLocationId]);

  return (
    <PageWrapper>
      {isStepEditable ? (
        <CountStepHeader
          printCountForm={printCountForm}
          next={() => validateExistenceOfCycleCounts(next)}
          save={handleCountStepHeaderSave}
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
      <VirtualizedTablesList
        cycleCountIds={cycleCountIds}
        isStepEditable={isStepEditable}
        isFormDisabled={isFormDisabled}
        isAssignCountModalOpen={isAssignCountModalOpen}
        closeAssignCountModal={closeAssignCountModal}
        assignCountModalData={assignCountModalData}
      />
    </PageWrapper>
  );
};

export default CountStep;
