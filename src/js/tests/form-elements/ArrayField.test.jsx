import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import TextField from 'components/form-elements/TextField';
import { renderFormField } from 'utils/form-utils';

import '@testing-library/jest-dom';

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
      map: (callback) => [`${name}[0]`, `${name}[1]`].map(callback),
      value: [{}, {}],
      length: 2,
      name: 'test-field',
    };
    return <Component fields={fields} {...others} />;
  },
  formValueSelector: () => {},
}));

jest.mock('react-localize-redux', () => ({
  ...jest.requireActual('react-localize-redux'),
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
  AutoSizer: (props) => (props.children({ width: 600 })),
  formValueSelector: () => {},
}));

describe('ArrayField component is correctly rendering', () => {
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

    render(<Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>);

    expect(screen.getByText('Add item')).toBeInTheDocument();
    expect(document.getElementById('test-field[0].textField')).toBeInTheDocument();
    expect(document.getElementById('test-field[1].textField2')).toBeInTheDocument();
    expect(screen.getAllByText('Delete')).toHaveLength(2);
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

    render(<Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>);

    expect(screen.queryByText('Add item')).not.toBeInTheDocument();
    expect(screen.getAllByTestId('input')).toHaveLength(4);
    expect(screen.getAllByText('Delete')).toHaveLength(2);
  });
});
