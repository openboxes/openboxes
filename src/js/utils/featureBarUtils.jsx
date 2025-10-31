import { InfoBar } from 'consts/infoBar';

// Create the feature bar if it has not been yet created
export const shouldCreateFullOutboundImportFeatureBar = (bars) =>
  !bars?.[InfoBar.FULL_OUTBOUND_IMPORT];

// Show the full outbound import feature bar if it has been created (added to store)
// and has not yet been closed by a user
export const shouldShowFullOutboundImportFeatureBar = (bars) =>
  bars?.[InfoBar.FULL_OUTBOUND_IMPORT] && !bars[InfoBar.FULL_OUTBOUND_IMPORT].closed;
