import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';

export const InfoBar = {
  FULL_OUTBOUND_IMPORT: 'FULL_OUTBOUND_IMPORT',
  STOCK_TRANSFER_DESCRIPTION: 'STOCK_TRANSFER_DESCRIPTION',
  STOCK_REPLENISHMENT_DESCRIPTION: 'STOCK_REPLENISHMENT_DESCRIPTION',
};

export const InfoBarConfigs = {
  [InfoBar.FULL_OUTBOUND_IMPORT]: {
    name: InfoBar.FULL_OUTBOUND_IMPORT,
    isCloseable: true,
    hasModalToDisplay: false,
    redirect: {
      label: 'react.infoBar.tryItNow.label',
      defaultLabel: 'Try it now!',
      link: STOCK_MOVEMENT_URL.importOutbound(),
    },
    versionLabel: {
      label: 'react.infoBar.version.new.label',
      defaultLabel: 'NEW!',
    },
    title: {
      label: 'react.infoBar.fullOutboundImport.title.label',
      defaultLabel: 'Do you know that you can now import a full outbound in one step from your packing list in a spreadsheet?',
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
