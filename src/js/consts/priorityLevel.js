// Ranges must match org.pih.warehouse.requisition.PriorityLevel
const PRIORITY_LEVELS = [
  { id: 'LOW', defaultLabel: 'Low', range: '< 0' },
  { id: 'NORMAL', defaultLabel: 'Normal', range: '0' },
  { id: 'MEDIUM', defaultLabel: 'Medium', range: '1-49' },
  { id: 'HIGH', defaultLabel: 'High', range: '50-99' },
  { id: 'CRITICAL', defaultLabel: 'Critical', range: '100+' },
];

const getPriorityLevelOptions = (translate) => PRIORITY_LEVELS
  .map(({ id, defaultLabel, range }) => ({
    id,
    value: id,
    label: `${translate(`react.stockMovement.priorityLevel.${id}.label`, defaultLabel)} (${range})`,
  }));

export default getPriorityLevelOptions;
