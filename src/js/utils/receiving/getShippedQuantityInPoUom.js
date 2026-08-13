/**
 * Shipped quantity in the purchase order's unit of measure: 7,500 each in packs of 100 reads
 * "75 PK/100".
 */
const getShippedQuantityInPoUom = ({ item, formatNumber }) => {
  const { quantityShipped, packSize, unitOfMeasure } = item || {};
  if (!unitOfMeasure || !packSize || quantityShipped == null) {
    return null;
  }
  return `${formatNumber(quantityShipped / packSize)} ${unitOfMeasure}`;
};

export default getShippedQuantityInPoUom;
