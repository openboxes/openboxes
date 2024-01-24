export const InfoBar = {
  AUTOSAVE: 'AUTOSAVE',
  STOCK_TRANSFER_DESCRIPTION: 'STOCK_TRANSFER_DESCRIPTION',
  STOCK_REPLENISHMENT_DESCRIPTION: 'STOCK_REPLENISHMENT_DESCRIPTION',
};

export const InfoBarConfigs = {
  [InfoBar.AUTOSAVE]: {
    name: InfoBar.AUTOSAVE,
    versionLabel: {
      label: 'react.infoBar.version.beta.label',
      defaultLabel: 'BETA',
    },
    title: {
      label: 'react.infoBar.autosave.title.label',
      defaultLabel: 'New autosave feature is here',
    },
  },
  [InfoBar.STOCK_TRANSFER_DESCRIPTION]: {
    name: InfoBar.STOCK_TRANSFER_DESCRIPTION,
    isCloseable: false,
    hasModalToDisplay: false,
    title: {
      label: 'react.infoBar.stockTransfer.instructions.label',
      defaultLabel: 'A Stock Transfer is an inventory tool that allows you to transfer stock from one bin location or zone to another. Use this inventory tool to reflect in OpenBoxes the physical location of inventory within your depot or warehouse.',
    },
  },
  [InfoBar.STOCK_REPLENISHMENT_DESCRIPTION]: {
    name: InfoBar.STOCK_REPLENISHMENT_DESCRIPTION,
    isCloseable: false,
    hasModalToDisplay: false,
    title: {
      label: 'react.infoBar.replenishment.instructions.label',
      defaultLabel: 'A Stock Replenishment is an inventory tool that allows you to replenish stock from a bulk section into a set of bins designated for picking. Use this inventory tool to replenish the quantity of a product in a bin with a certain min or max level that you want to maintain.',
    },
  },
};

export const InfoBarVersionBoxVariant = {
  FILLED: 'filled',
  OUTLINED: 'outlined',
};
