import { useMemo, useState } from 'react';

import PropTypes from 'prop-types';

const useWizard = ({ initialKey, steps }) => {
  const [key, setKey] = useState(initialKey);

  const stepProperties = useMemo(() => {
    const foundStepIdx = steps.findIndex((s) => s.key === key);
    // findIndex returns -1 if the index is not found for given predicate
    if (foundStepIdx === -1) {
      throw new Error('Wizard step has not been found!');
    }
    return {
      Step: steps[foundStepIdx],
      currentStepIdx: foundStepIdx,
    };
  }, [key, initialKey]);

  const first = () => {
    setKey(steps[0]?.key);
  };

  const last = () => {
    const lastIdx = steps.length - 1;
    setKey(steps[lastIdx]?.key);
  };

  const next = () => {
    const nextStepIdx = stepProperties.currentStepIdx + 1;
    const nextStep = steps[nextStepIdx];
    if (nextStep) {
      setKey(nextStep.key);
    }
  };

  const previous = () => {
    const previousStepIdx = stepProperties.currentStepIdx - 1;
    if (previousStepIdx >= 0) {
      setKey(steps[previousStepIdx]?.key);
    }
  };

  const is = (stepKey) => key === stepKey;

  const { Step } = stepProperties;

  return [
    Step,
    {
      set: setKey,
      first,
      next,
      previous,
      last,
      is,
    },
  ];
};

export default useWizard;

useWizard.propTypes = {
  initialKey: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.string,
  ]).isRequired,
  steps: PropTypes.arrayOf(
    PropTypes.shape({
      title: PropTypes.string.isRequired,
      key: PropTypes.oneOfType([
        PropTypes.number,
        PropTypes.string,
      ]).isRequired,
      Component: PropTypes.node.isRequired,
    }),
  ).isRequired,
};
