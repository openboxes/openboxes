import React from 'react';
import renderer from 'react-test-renderer';
import ArrayField from '../../components/form-elements/ArrayField';
import TextField from '../../components/form-elements/TextField';
import ButtonField from '../../components/form-elements/ButtonField';
import { renderFormField } from '../../utils/form-utils';

jest.mock('react-final-form', () => ({
  Field: (props) => {
    const { component: Component, name, ...others } = props;
    const input = { onChange: () => {}, name };
    const meta = {};
    return <Component input={input} meta={meta} {...others} />;
  },
}));

jest.mock('react-final-form-arrays', () => ({
  FieldArray: (props) => {
    const { component: Component, name, ...others } = props;
    const fields = {
      map: callback => [`${name}[0]`, `${name}[1]`].map(callback),
      value: [{}, {}],
      length: 2,
      name: 'test-field',
    };
    return <Component fields={fields} {...others} />;
  },
  formValueSelector: () => {},
}));

jest.mock('react-localize-redux', () => ({
  Translate: (props) => {
    const { id } = props;

    return `${id}`;
  },
}));

jest.mock('react-virtualized', () => ({
  List: (props) => {
    const { rowCount, rowRenderer } = props;

    return (
      [...new Array(rowCount)].map((e, index) => (rowRenderer({
        key: index, index, style: {}, isScrolling: false, isVisible: true,
      })))
    );
  },
  AutoSizer: props => (props.children({ width: 600 })),
  formValueSelector: () => {},
}));

jest.mock('react-tippy');

xdescribe('ArrayField component is correctly rendering', () => {
  it('with Add button', () => {
    const fieldConfig = {
      type: ArrayField,
      addButton: 'Add item',
      fields: {
        textField: {
          type: TextField,
          label: 'Name',
        },
        textField2: {
          type: TextField,
          label: 'Name',
        },
        button: {
          type: ButtonField,
          buttonLabel: 'Delete',
          getDynamicAttr: ({ removeRow }) => ({
            onClick: removeRow,
          }),
          attributes: {
            className: 'btn btn-outline-danger',
          },
        },
      },
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });

  it('with no Add button', () => {
    const fieldConfig = {
      type: ArrayField,
      fields: {
        textField: {
          type: TextField,
          label: 'Name',
        },
        textField2: {
          type: TextField,
          label: 'Name',
        },
        button: {
          type: ButtonField,
          buttonLabel: 'Delete',
          getDynamicAttr: ({ removeRow }) => ({
            onClick: removeRow,
          }),
          attributes: {
            className: 'btn btn-outline-danger',
          },
        },
      },
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

