import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import useQueryParams from 'hooks/useQueryParams';
import { isCounting, isResolving } from 'utils/checkCycleCountStatus';

const useCycleCountProductAvailability = (row) => {
  const { tab } = useQueryParams();

  if (isCounting(row.status) && tab !== TO_COUNT_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.counting.label',
      defaultMessage: 'Count started on the product: find the product on To Count tab',
      isCheckboxChecked: true,
    };
  }

  if (isResolving(row.status) && tab !== TO_RESOLVE_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.resolving.label',
      defaultMessage: 'Resolution started on the product: find the product on To Resolve tab',
      isCheckboxChecked: true,
    };
  }

  if (row.quantityAllocated > 0 && tab === TO_COUNT_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.label',
      defaultMessage: 'Cannot start count on this product with pending stock movement',
      isCheckboxChecked: false,
    };
  }

  if (row.quantityAllocated > 0 && tab === TO_RESOLVE_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.warning',
      defaultMessage: 'This product is in a pending stock movement. Check quantity input carefully',
      isCheckboxChecked: false,
    };
  }

  if (row.quantityAllocated > 0) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.label',
      defaultMessage: 'Cannot start count on this product with pending stock movement',
      isCheckboxChecked: false,
    };
  }

  return {
    isProductDisabled: false,
    label: null,
    defaultMessage: null,
    isCheckboxChecked: false,
  };
};

export default useCycleCountProductAvailability;
