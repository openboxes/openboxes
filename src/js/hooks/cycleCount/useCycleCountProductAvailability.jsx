import { isCounting, isResolving } from 'utils/checkCycleCountStatus';

const useCycleCountProductAvailability = (status) => {
  if (isCounting(status)) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.counting.label',
      defaultMessage: 'Count started on the product: find the product on To Count tab',
    };
  }

  if (isResolving(status)) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.resolving.label',
      defaultMessage: 'Resolution started on the product: find the product on To Resolve tab',
    };
  }

  return {
    isProductDisabled: false,
    label: null,
    defaultMessage: null,
  };
};

export default useCycleCountProductAvailability;
