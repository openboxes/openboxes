import React from 'react';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import ResolveStepHeader from 'components/cycleCount/toResolveTab/ResolveStepHeader';
import ResolveStepTable from 'components/cycleCount/toResolveTab/ResolveStepTable';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const ResolveStep = () => {
  const {
    tableData,
    validationErrors,
    printRecountForm,
    refreshCountItems,
    disableNextButton,
    next,
    tableMeta,
    addEmptyRow,
    removeRow,
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    shouldHaveRootCause,
    back,
    save,
    isStepEditable,
    getRecountedBy,
    getCountedBy,
    submitRecount,
    getProduct,
    getDateCounted,
    isFormValid,
    refreshFocusCounter,
    triggerValidation,
    isSaveDisabled,
    setIsSaveDisabled,
    cycleCountsWithItemsWithoutRecount,
  } = useResolveStep();
  useTranslation('cycleCount');

  return (
    <PageWrapper>
      {isStepEditable ? (
        <ResolveStepHeader
          printRecountForm={printRecountForm}
          refreshCountItems={refreshCountItems}
          disableNextButton={disableNextButton}
          next={next}
          save={save}
        />
      ) : (
        <ConfirmStepHeader
          back={back}
          save={submitRecount}
          isSaveDisabled={isSaveDisabled}
          setIsSaveDisabled={setIsSaveDisabled}
        />
      )}
      {tableData
        .map(({ cycleCountItems, id }) => (
          <ResolveStepTable
            key={id}
            id={id}
            product={getProduct(id)}
            dateCounted={getDateCounted(id)}
            dateRecounted={getRecountedDate(id)}
            tableData={cycleCountItems}
            tableMeta={tableMeta}
            addEmptyRow={addEmptyRow}
            removeRow={removeRow}
            setRecountedDate={setRecountedDate(id)}
            assignRecountedBy={assignRecountedBy}
            validationErrors={validationErrors}
            shouldHaveRootCause={shouldHaveRootCause}
            isStepEditable={isStepEditable}
            recountedBy={getRecountedBy(id)}
            countedBy={getCountedBy(id)}
            isFormValid={isFormValid}
            triggerValidation={triggerValidation}
            refreshFocusCounter={refreshFocusCounter}
            cycleCountsWithItemsWithoutRecount={
              cycleCountsWithItemsWithoutRecount.find((cycleCount) => cycleCount.id === id)
            }
          />
        ))}
    </PageWrapper>
  );
};

export default ResolveStep;
