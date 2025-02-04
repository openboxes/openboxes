import React from 'react';

import CountStepHeader from 'components/cycleCount/toCountTab/CountStepHeader';
import CountStepTable from 'components/cycleCount/toCountTab/CountStepTable';
import useCountStep from 'hooks/cycleCount/useCountStep';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CountStep = () => {
  const {
    dataGroupedByTables,
    printCountForm,
    next,
    tableMeta,
    addEmptyRow,
    removeRow,
    assignCountedBy,
    validationErrors,
  } = useCountStep();

  return (
    <PageWrapper>
      <CountStepHeader
        printCountForm={printCountForm}
        next={next}
      />
      {Object.values(dataGroupedByTables)
        .map((data) => (
          <CountStepTable
            product={data[0]?.product}
            dateCounted={data[0]?.dateCounted}
            tableData={data}
            tableMeta={tableMeta}
            addEmptyRow={addEmptyRow}
            removeRow={removeRow}
            assignCountedBy={assignCountedBy}
            validationErrors={validationErrors}
          />
        ))}
    </PageWrapper>
  );
};

export default CountStep;
