import React from 'react';

import ConfirmStepHeader from 'components/cycleCount/toCountTab/ConfirmStepHeader';
import CountStepHeader from 'components/cycleCount/toCountTab/CountStepHeader';
import CountStepTable from 'components/cycleCount/toCountTab/CountStepTable';
import useCountStep from 'hooks/cycleCount/useCountStep';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const CountStep = () => {
  const {
    tableData,
    printCountForm,
    next,
    save,
    back,
    tableMeta,
    addEmptyRow,
    removeRow,
    assignCountedBy,
    getCountedDate,
    setCountedDate,
    validationErrors,
    isEditable,
    countedBy,
  } = useCountStep();

  return (
    <PageWrapper>
      {isEditable ? (
        <CountStepHeader
          printCountForm={printCountForm}
          next={next}
        />
      ) : <ConfirmStepHeader back={back} save={save} />}
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
            isEditable={isEditable}
            countedBy={countedBy}
          />
        ))}
    </PageWrapper>
  );
};

export default CountStep;
