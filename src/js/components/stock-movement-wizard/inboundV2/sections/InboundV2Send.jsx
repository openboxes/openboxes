import React from 'react';

import PropTypes from 'prop-types';

import InboundSendForm from 'components/stock-movement-wizard/inboundV2/sections/InboundSendForm';

const InboundV2Send = ({ previous }) => (
  <div>
    <InboundSendForm previous={previous} />
  </div>
);

export default InboundV2Send;

InboundV2Send.propTypes = {
  previous: PropTypes.func.isRequired,
};
