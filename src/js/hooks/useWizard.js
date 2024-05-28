import { useMemo, useState } from 'react';

import PropTypes from 'prop-types';

const useWizard = ({ initialKey, steps }) => {
  const [key, setKey] = useState(initialKey);

  const set = (val) => {
    setKey(val);
  };

  const first = () => {
    setKey(steps[0]?.key);
  };

  const last = () => {
    const lastIdx = steps.length - 1;
    setKey(steps[lastIdx]?.key);
  };

  const next = () => {
    const nextStepIdx = steps.findIndex((s) => s.key === key) + 1;
    const nextStep = steps[nextStepIdx];
    if (nextStep) {
      setKey(nextStep.key);
    }
  };

  const previous = () => {
    const previousStepIdx = steps.findIndex((s) => s.key === key) - 1;
    if (previousStepIdx >= 0) {
      setKey(steps[previousStepIdx]?.key);
    }
  };

  const Step = useMemo(() => {
    const foundStep = steps.find((s) => s.key === key);
    if (!foundStep) {
      throw new Error('Wizard step has not been found!');
    }
    return foundStep;
  }, [key, initialKey]);

  return [
    Step,
    {
      set,
      first,
      next,
      previous,
      last,
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
