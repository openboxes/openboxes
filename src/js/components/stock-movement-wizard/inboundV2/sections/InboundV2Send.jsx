import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import InboundV2AddItems
  from 'components/stock-movement-wizard/inboundV2/sections/InboundV2AddItems';

const InboundV2Send = ({ previous }) => {
  console.log('InboundV2Send');
  return (
    <>
      <Section>
        InboundV2Send
      </Section>
      <Button
        label="react.default.button.previous.label"
        defaultLabel="Previous"
        variant="primary"
        className="fit-content align-self-end"
        onClick={() => previous()}
      />
    </>
  );
};

export default InboundV2Send;

InboundV2AddItems.propTypes = {
  previous: PropTypes.func.isRequired,
};
