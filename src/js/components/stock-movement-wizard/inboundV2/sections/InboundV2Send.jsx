import React from 'react';

import PropTypes from 'prop-types';

import InboundSendForm from 'components/stock-movement-wizard/inboundV2/sections/InboundSendForm';
import InboundSendTable from 'components/stock-movement-wizard/inboundV2/sections/InboundSendTable';

const InboundV2Send = ({ previous }) => (
  <div className="mb-4">
    <InboundSendForm previous={previous} />
    <InboundSendTable />
  </div>
);

export default InboundV2Send;

InboundV2Send.propTypes = {
  previous: PropTypes.func.isRequired,
};
