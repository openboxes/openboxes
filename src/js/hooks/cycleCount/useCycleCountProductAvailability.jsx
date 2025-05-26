import CycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';

const useCycleCountProductAvailability = (status) => {
  if (
    status === CycleCountCandidateStatus.CREATED
    || status === CycleCountCandidateStatus.REQUESTED
    || status === CycleCountCandidateStatus.COUNTING
  ) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.counting.label',
      defaultMessage: 'Count started on the product: find the product on To Count tab',
    };
  }

  if (
    status === CycleCountCandidateStatus.COUNTED
    || status === CycleCountCandidateStatus.INVESTIGATING
  ) {
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
