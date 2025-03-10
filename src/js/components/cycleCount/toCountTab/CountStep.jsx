import React from 'react';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
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
    isFormValid,
    focusProps,
  } = useCountStep();

  return (
    <PageWrapper>
      {isStepEditable ? (
        <CountStepHeader
          printCountForm={printCountForm}
          next={next}
          save={save}
        />
      ) : (
        <ConfirmStepHeader
          back={back}
          save={resolveDiscrepancies}
        />
      )}
      {tableData
        .map(({ cycleCountItems, id }, index) => (
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
            isFormValid={isFormValid}
            focusProps={focusProps}
            tableIndex={index}
          />
        ))}
    </PageWrapper>
  );
};

export default CountStep;
