import { ALL_PRODUCTS_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import useQueryParams from 'hooks/useQueryParams';
import { isCounting, isResolving } from 'utils/checkCycleCountStatus';

const useCycleCountProductAvailability = (row) => {
  const { tab } = useQueryParams();

  if (isCounting(row.status) && tab === ALL_PRODUCTS_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.counting.label',
      defaultMessage: 'Count started on the product: find the product on To Count tab',
      isFromOtherTab: true,
    };
  }

  if (isResolving(row.status) && tab === ALL_PRODUCTS_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.status.resolving.label',
      defaultMessage: 'Resolution started on the product: find the product on To Resolve tab',
      isFromOtherTab: true,
    };
  }

  if (row.quantityAllocated > 0 && tab === TO_RESOLVE_TAB) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.warning.label',
      defaultMessage: 'This product is in a pending stock movement. Check quantity input carefully',
      isFromOtherTab: false,
    };
  }

  if (row.quantityAllocated > 0) {
    return {
      isProductDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.info.label',
      defaultMessage: 'Cannot start count on this product with pending stock movement',
      isFromOtherTab: false,
    };
  }

  return {
    isProductDisabled: false,
    label: null,
    defaultMessage: null,
    isFromOtherTab: false,
  };
};

export default useCycleCountProductAvailability;
