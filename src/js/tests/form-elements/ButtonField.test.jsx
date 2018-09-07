import React from 'react';
import renderer from 'react-test-renderer';
import ButtonField from '../../components/form-elements/ButtonField';
import { renderFormField } from '../../utils/form-utils';

describe('ButtonField component is correctly rendering', () => {
  it('string label', () => {
    const fieldConfig = {
      type: ButtonField,
      buttonLabel: 'button label',
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });

  it('component label', () => {
    const fieldConfig = {
      type: ButtonField,
      buttonLabel: () => <span>test label</span>,
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

