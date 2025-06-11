const cycleCountReportingIndicators = {
  totalCount: {
    title: 'react.cycleCount.indicators.cardTitle.totalCount.label',
    titleDefaultValue: 'Total count physical inventory',
    numberType: 'number',
    subtitle: 'react.cycleCount.indicators.cardSubtitle.inMyInventory.label',
    subtitleDefaultValue: 'in my inventory',
    info: 'react.cycleCount.indicators.cardInfo.totalCount.label',
    infoDefaultValue: 'Total physical inventory count',
  },
  itemsCounted: {
    title: 'react.cycleCount.indicators.cardTitle.itemsCounted.label',
    titleDefaultValue: 'Items counted',
    numberType: 'number',
    subtitle: 'react.cycleCount.indicators.cardSubtitle.inMyInventory.label',
    subtitleDefaultValue: 'in my inventory',
    info: 'react.cycleCount.indicators.cardInfo.itemsCounted.label',
    infoDefaultValue: 'Total items counted',
  },
  targetProgress: {
    title: 'react.cycleCount.indicators.cardTitle.targetProgress.label',
    titleDefaultValue: 'Target progress',
    numberType: 'number',
    subtitle: 'react.cycleCount.indicators.cardSubtitle.itemsCounted.label',
    subtitleDefaultValue: 'items counted',
    info: 'react.cycleCount.indicators.cardInfo.targetProgress.label',
    infoDefaultValue: 'Shows how far along the count progress is',
    showPercentSign: true,
  },
  notFinishedItems: {
    title: 'react.cycleCount.indicators.cardTitle.notFinishedItems.label',
    titleDefaultValue: 'Not finished items',
    numberType: 'number',
    subtitle: 'react.cycleCount.indicators.cardSubtitle.inMyInventory.label',
    subtitleDefaultValue: 'in my inventory',
    info: 'react.cycleCount.indicators.cardInfo.notFinishedItems.label',
    infoDefaultValue: 'Items remaining to be counted or processed',
  },
};

export default cycleCountReportingIndicators;
