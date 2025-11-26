import CycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';

const COUNTING_STATUSES = [
  CycleCountCandidateStatus.CREATED,
  CycleCountCandidateStatus.REQUESTED,
  CycleCountCandidateStatus.COUNTING,
];

const RESOLVING_STATUSES = [
  CycleCountCandidateStatus.COUNTED,
  CycleCountCandidateStatus.INVESTIGATING,
];

export const isCounting = (status) => COUNTING_STATUSES.includes(status);
export const isResolving = (status) => RESOLVING_STATUSES.includes(status);
