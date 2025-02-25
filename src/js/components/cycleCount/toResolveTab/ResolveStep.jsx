import React from 'react';

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
  } = useResolveStep();

  return (
    <PageWrapper>
      <ResolveStepHeader printRecountForm={printRecountForm} next={next} />
      {tableData
        .map(({ cycleCountItems, id }) => (
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
          />
        ))}
    </PageWrapper>
  );
};

export default ResolveStep;
