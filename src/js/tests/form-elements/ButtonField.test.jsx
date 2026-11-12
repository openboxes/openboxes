import React from 'react';

import { render } from '@testing-library/react';

import ButtonField from 'components/form-elements/ButtonField';
import { renderFormField } from 'utils/form-utils';

jest.mock('react-localize-redux', () => ({
  Translate: (props) => {
    const { id } = props;

    return `${id}`;
  },
}));

describe('ButtonField component is correctly rendering', () => {
  it('string label', () => {
    const fieldConfig = {
      type: ButtonField,
      buttonLabel: 'button label',
    };

    const rendered = render(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.asFragment()).toMatchSnapshot();
  });

  it('component label', () => {
    const fieldConfig = {
      type: ButtonField,
      buttonLabel: () => <span>test label</span>,
    };

    const rendered = render(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.asFragment()).toMatchSnapshot();
  });
});
