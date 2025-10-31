import { STOCK_MOVEMENT_ITEM_REVERT_PICK } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  revertPick: (itemId) => apiClient.delete(STOCK_MOVEMENT_ITEM_REVERT_PICK(itemId)),
};
