import React from 'react';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import ResolveStepHeader from 'components/cycleCount/toResolveTab/ResolveStepHeader';
import ResolveStepTable from 'components/cycleCount/toResolveTab/ResolveStepTable';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const ResolveStep = () => {
  const {
    tableData,
    validationErrors,
    printRecountForm,
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
    focusProps,
  } = useResolveStep();

  return (
    <PageWrapper>
      {isStepEditable ? (
        <ResolveStepHeader
          printRecountForm={printRecountForm}
          next={next}
        />
      ) : <ConfirmStepHeader back={back} save={save} />}
      {tableData
        .map(({ cycleCountItems, id }, index) => (
          <ResolveStepTable
            key={id}
            id={id}
            product={cycleCountItems[0]?.product}
            dateCounted={cycleCountItems[0]?.dateCounted}
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
            focusProps={focusProps}
            tableIndex={index}
          />
        ))}
    </PageWrapper>
  );
};

export default ResolveStep;
