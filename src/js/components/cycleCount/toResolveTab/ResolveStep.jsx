import React, { useState } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCycleCountsIds } from 'selectors';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import ResolveStepHeader from 'components/cycleCount/toResolveTab/ResolveStepHeader';
import ResolveStepTable from 'components/cycleCount/toResolveTab/ResolveStepTable';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const ResolveStep = () => {
  const cycleCountIds = useSelector(getCycleCountsIds);
  const [currentCycleCountIds] = useState(cycleCountIds);
  const isUIBlocked = !_.isEqual(cycleCountIds, currentCycleCountIds);
  const {
    tableData,
    validationErrors,
    printRecountForm,
    refreshCountItems,
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
          next={next}
          save={save}
          isUIBlocked={isUIBlocked}
        />
      ) : (
        <ConfirmStepHeader
          back={back}
          save={submitRecount}
          isSaveDisabled={isSaveDisabled}
          setIsSaveDisabled={setIsSaveDisabled}
          isUIBlocked={isUIBlocked}
          redirectTab={TO_RESOLVE_TAB}
          redirectLabel="react.cycleCount.redirectToResolveTab.label"
          redirectDefaultMessage="Back to Resolve tab"
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
            cycleCountWithItemsWithoutRecount={
              cycleCountsWithItemsWithoutRecount.find((cycleCount) => cycleCount.id === id)
            }
            isUIBlocked={isUIBlocked}
          />
        ))}
    </PageWrapper>
  );
};

export default ResolveStep;
