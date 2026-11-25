import _ from 'lodash';

import { receiptGroupForView } from 'utils/receiving/receiptSummaryRows';

// Query of the receipt summary endpoint, shared by the receiving and the check step.
// Backend binds `sort` as a SortParamList: "field" for ascending, "-field" for descending.
const getReceiptSummaryParams = ({ view, sort, sortOrder } = {}) => _.omitBy({
  group: receiptGroupForView(view),
  sort: sort && `${sortOrder === 'desc' ? '-' : ''}${sort}`,
}, _.isEmpty);

export default getReceiptSummaryParams;
