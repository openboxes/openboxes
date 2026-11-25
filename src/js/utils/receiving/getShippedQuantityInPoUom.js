/**
 * Shipped quantity in the purchase order's unit of measure. For example, 1,044 each in packs
 * of 2 gives { quantity: "522", unitOfMeasure: "PK/2", label: "522 PK/2" }.
 */
const getShippedQuantityInPoUom = ({ item, formatNumber }) => {
  const { quantityShipped, packSize, unitOfMeasure } = item || {};
  if (!unitOfMeasure || !packSize || quantityShipped == null) {
    return null;
  }
  const quantity = formatNumber(quantityShipped / packSize);
  return {
    quantity,
    unitOfMeasure,
    label: `${quantity} ${unitOfMeasure}`,
  };
};

export default getShippedQuantityInPoUom;
