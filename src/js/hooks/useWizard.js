import { useLayoutEffect, useMemo } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';
import useScrollbar from 'hooks/useScrollbar';

const useWizard = ({ initialKey, steps }) => {
  const parsedQueryParams = useQueryParams();
  const history = useHistory();
  const location = useLocation();

  /**
   * Navigate to a specific wizard step.
   *
   * @param {Object} params - The parameters object.
   * @param {string} params.step - Step key to navigate to.
   * @param {Object} params.queryParams - Optional extra query params to merge into the URL.
   * @param {string|null} params.pathId - Optional id from `useParams()` hook from
   * 'react-router-dom' library to append to the pathname.
   */
  const navigateToStep = ({ step, queryParams = {}, pathId = null } = {}) => {
    const currentPath = location.pathname;
    // We split the current path to get the last segment because last element can be already
    // our pathId
    const segments = currentPath.split('/').filter(Boolean);
    const lastSegment = segments[segments.length - 1];
    // Trim possible trailing slash to avoid double slashes in the URL
    const trimmedPath = currentPath.endsWith('/') ? currentPath.slice(0, -1) : currentPath;

    // Now we check if we passed a pathId and if it's different from the last segment
    // of the current path to avoid duplicating the id in the URL.
    const targetPath = pathId && lastSegment !== pathId ? `${trimmedPath}/${pathId}` : currentPath;

    history.push(
      queryString.stringifyUrl(
        {
          url: targetPath,
          query: { ...parsedQueryParams, step, ...queryParams },
        },
      ),
    );
  };

  /** Compute current active wizard step
   * Determine current wizard step based on URl query "step" parameter
   * Otherwise use provided initial key
   * Or default to the first step
   */
  const currentStepKey = useMemo(() =>
    parsedQueryParams.step || initialKey || steps[0]?.key,
  [parsedQueryParams.step, steps, initialKey]);

  const { scrollToTop } = useScrollbar({
    selector: 'body',
  });

  useLayoutEffect(() => {
    scrollToTop();
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
    navigateToStep({ step: steps[0]?.key });
  };

  const last = () => {
    const lastIdx = steps.length - 1;
    navigateToStep({ step: steps[lastIdx]?.key });
  };

  // params might be needed to join some query params to the URL while switching the step
  const next = ({ queryParams = {}, pathId = null } = {}) => {
    const nextStepIdx = stepProperties.currentStepIdx + 1;
    const nextStep = steps[nextStepIdx];
    if (nextStep) {
      navigateToStep({ step: nextStep.key, queryParams, pathId });
    }
  };

  const previous = () => {
    const previousStepIdx = stepProperties.currentStepIdx - 1;
    if (previousStepIdx >= 0) {
      navigateToStep({ step: steps[previousStepIdx]?.key });
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
