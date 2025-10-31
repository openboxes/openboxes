import React from 'react';

import PropTypes from 'prop-types';

import InboundSendForm from 'components/stock-movement-wizard/inboundV2/sections/InboundSendForm';
import InboundSendTable from 'components/stock-movement-wizard/inboundV2/sections/InboundSendTable';

const InboundSend = ({ previous }) => (
  <div className="inbound-send-shipment">
    <InboundSendForm previous={previous} />
    <InboundSendTable />
  </div>
);

export default InboundSend;

InboundSend.propTypes = {
  previous: PropTypes.func.isRequired,
};
