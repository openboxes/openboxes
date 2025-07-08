const cycleCountReportingIndicators = {
  productsInventoried: {
    titleLabel: 'react.cycleCount.indicators.cardTitle.productsInventoried.label',
    defaultTitle: 'Products inventoried',
    numberType: 'number',
    infoLabel: 'react.cycleCount.indicators.cardInfo.productsInventoried.label',
    defaultInfo: 'Number of products that were counted at least once during the time range',
  },
  inventoryAccuracy: {
    titleLabel: 'react.cycleCount.indicators.cardTitle.inventoryAccuracy.label',
    defaultTitle: 'Not finished items',
    numberType: 'number',
    infoLabel: 'react.cycleCount.indicators.cardInfo.inventoryAccuracy.label',
    defaultInfo: 'Percentage of products cycle counted with an equal count, compared to all products cycle counted during the time range (equal and non equal counts). This represents the share of inventory for which OpenBoxes inventory is accurately representing the physical inventory.',
    showPercentSign: true,
  },
  inventoryLoss: {
    titleLabel: 'react.cycleCount.indicators.cardTitle.inventoryLoss.label',
    defaultTitle: 'Inventory loss',
    firstSubtitleLabel: 'react.cycleCount.indicators.cardSubtitle.countedLess.label',
    defaultFirstSubtitle: 'counted less than in system',
    secondSubtitleLabel: 'react.cycleCount.indicators.cardSubtitle.overallLost.label',
    defaultSecondSubtitle: 'overall financial value lost',
    infoLabel: 'react.cycleCount.indicators.cardInfo.inventoryLoss.label',
    defaultInfo: 'Inventory loss count is the number of products for which the overall inventory change over the time range selected is a negative adjustment (qty at the beginning of the time range is more than qty at the end). The financial inventory loss is the equivalent of the inventory decrease for these products in USD.',
  },
};

export default cycleCountReportingIndicators;
