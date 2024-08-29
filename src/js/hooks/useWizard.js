import { useLayoutEffect, useMemo } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';
import useResetScrollbar from 'hooks/useResetScrollbar';

const useWizard = ({ initialKey, steps }) => {
  const parsedQueryParams = useQueryParams();
  const history = useHistory();
  const location = useLocation();

  const navigateToStep = (step) => {
    history.push(queryString.stringifyUrl({
      url: location.pathname,
      query: { ...parsedQueryParams, step },
    }));
  };

  /** Compute current active wizard step
   * Determine current wizard step based on URl query "step" parameter
   * Otherwise use provided initial key
   * Or default to the first step
   */
  const currentStepKey = useMemo(() =>
    parsedQueryParams.step || initialKey || steps[0]?.key,
  [parsedQueryParams.step, steps, initialKey]);

  const { resetScrollbar } = useResetScrollbar({
    selector: 'body',
  });

  useLayoutEffect(() => {
    resetScrollbar();
  }, [currentStepKey]);

  const stepProperties = useMemo(() => {
    let foundStepIdx = steps.findIndex((s) => s.key === currentStepKey);
    // findIndex returns -1 if the index is not found for given predicate
    // default to first step on index 0
    if (foundStepIdx === -1) {
      foundStepIdx = 0;
    }
    return {
      key: steps[foundStepIdx]?.key,
      Step: steps[foundStepIdx],
      currentStepIdx: foundStepIdx,
    };
  }, [currentStepKey]);

  const first = () => {
    navigateToStep(steps[0]?.key);
  };

  const last = () => {
    const lastIdx = steps.length - 1;
    navigateToStep(steps[lastIdx]?.key);
  };

  const next = () => {
    const nextStepIdx = stepProperties.currentStepIdx + 1;
    const nextStep = steps[nextStepIdx];
    if (nextStep) {
      navigateToStep(nextStep.key);
    }
  };

  const previous = () => {
    const previousStepIdx = stepProperties.currentStepIdx - 1;
    if (previousStepIdx >= 0) {
      navigateToStep(steps[previousStepIdx]?.key);
    }
  };

  const is = (stepKey) => stepProperties?.key === stepKey;

  const { Step } = stepProperties;

  return [
    Step,
    {
      navigateToStep,
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
      key: PropTypes.oneOfType([
        PropTypes.number,
        PropTypes.string,
      ]).isRequired,
      Component: PropTypes.node.isRequired,
    }),
  ).isRequired,
};
