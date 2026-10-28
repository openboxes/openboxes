import React from 'react';

import { createPortal } from 'react-dom';
import { Tooltip } from 'react-tooltip';

const PortalTooltip = (props) => createPortal(<Tooltip {...props} />, document.body);

export default PortalTooltip;
