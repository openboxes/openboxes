import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import Modal from 'react-modal';

import { renderFormField } from 'utils/form-utils';
import Translate from 'utils/Translate';

class ModalWrapper extends Component {
  constructor(props) {
    super(props);

    this.state = {
      showModal: false,
    };

    this.openModal = this.openModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
    this.save = this.save.bind(this);
  }

  openModal() {
    this.props.onOpen();
    this.setState({ showModal: true });
  }

  closeModal() {
    this.props.onClose();
    this.setState({ showModal: false });
  }

  save(values) {
    this.props.onSave(values);
    this.setState({ showModal: false });
  }

  render() {
    const Title = this.props.title;
    const { defaultTitleMessage } = this.props;

    return (
      <div className={this.props.wrapperClassName}>
        {
          this.props.renderButton
            ? this.props.renderButton({ openModal: this.openModal })
            : (
              <button
                type="button"
                className={`btn-xs ${this.props.btnOpenClassName}`}
                style={this.props.btnOpenStyle}
                disabled={this.props.btnOpenDisabled}
                onClick={() => this.openModal()}
              >
                {
                  this.props.btnOpenIcon
                  && <i className={`fa ${this.props.btnOpenIcon} mr-1`} aria-hidden="true" />
                }
                {
                  this.props.btnOpenText && !this.props.btnOpenAsIcon
                  && (
                  <Translate
                    id={this.props.btnOpenText}
                    defaultMessage={this.props.btnOpenDefaultText}
                  />
                  )
                }
              </button>
            )
        }
        <Modal
          isOpen={this.props.showModal || this.state.showModal}
          onRequestClose={this.closeModal}
          className="modal-content-custom"
          shouldCloseOnOverlayClick={false}
        >
          <div className={this.props.bodyContainerClassName} style={this.props.bodyContainerStyle}>
            {
            typeof Title === 'string'
              ? (
                <h5 className="text-center">
                  {' '}
                  <Translate id={Title} defaultMessage={defaultTitleMessage} />
                </h5>
              )
              : <Title />
          }
            <hr />
            <Form
              onSubmit={(values) => this.save(values)}
              initialValues={this.props.initialValues}
              validate={this.props.validate}
              mutators={{ ...arrayMutators }}
              render={({ handleSubmit, values }) =>
                (
                  <form id="modalForm" onSubmit={handleSubmit}>

                    {this.props.children}
                    {this.props.renderBodyWithValues(values)}

                    {_.map(
                      this.props.fields,
                      (fieldConfig, fieldName) =>
                        renderFormField(
                          fieldConfig,
                          fieldName,
                          { ...this.props.formProps, values },
                        ),
                    )}

                    <hr />

                    <div
                      className={this.props.btnContainerClassName}
                      role="group"
                      style={this.props.btnContainerStyle}
                    >
                      <button
                        type="button"
                        className={this.props.btnCancelClassName}
                        style={this.props.btnCancelStyle}
                        onClick={() => this.closeModal()}
                      >
                        <Translate
                          id={this.props.btnCancelText}
                          defaultMessage={this.props.btnCancelDefaultText}
                        />
                      </button>
                      <button
                        type="submit"
                        className={this.props.btnSaveClassName}
                        style={this.props.btnSaveStyle}
                        disabled={this.props.btnSaveDisabled}
                      >
                        <Translate
                          id={this.props.btnSaveText}
                          defaultMessage={this.props.btnSaveDefaultText}
                        />
                      </button>
                    </div>
                  </form>
                )}
            />
          </div>
        </Modal>
      </div>
    );
  }
}

export default ModalWrapper;

ModalWrapper.propTypes = {
  /** Open button properties */
  btnOpenText: PropTypes.string,
  btnOpenDefaultText: PropTypes.string,
  btnOpenClassName: PropTypes.string,
  btnOpenStyle: PropTypes.shape({}),
  btnOpenDisabled: PropTypes.bool,
  btnOpenAsIcon: PropTypes.bool,
  btnOpenIcon: PropTypes.string,

  /** Modal title property */
  title: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func,
  ]).isRequired,
  defaultTitleMessage: PropTypes.string.isRequired,

  /** Modal body container properties */
  children: PropTypes.element,
  bodyContainerClassName: PropTypes.string,
  bodyContainerStyle: PropTypes.shape({}),

  /** Button container properties */
  btnContainerClassName: PropTypes.string,
  btnContainerStyle: PropTypes.shape({}),
  renderButton: PropTypes.func,

  /** Wrapper properties */
  wrapperClassName: PropTypes.string,

  /** Save button properties */
  btnSaveText: PropTypes.string,
  btnSaveDefaultText: PropTypes.string,
  btnSaveClassName: PropTypes.string,
  btnSaveStyle: PropTypes.shape({}),
  btnSaveDisabled: PropTypes.bool,

  /** Cancel button properties */
  btnCancelText: PropTypes.string,
  btnCancelDefaultText: PropTypes.string,
  btnCancelClassName: PropTypes.string,
  btnCancelStyle: PropTypes.shape({}),

  /** Functional properties */
  onOpen: PropTypes.func,
  onSave: PropTypes.func,
  onClose: PropTypes.func,

  /** Form elements */
  validate: PropTypes.func,
  renderBodyWithValues: PropTypes.func,
  initialValues: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]),
  fields: PropTypes.shape({}),
  formProps: PropTypes.shape({}),
  showModal: PropTypes.bool,
};

ModalWrapper.defaultProps = {
  btnOpenText: 'react.default.button.open.label',
  btnOpenDefaultText: 'Open',
  btnOpenClassName: 'btn btn-outline-primary',
  btnOpenStyle: {},
  btnOpenDisabled: false,
  btnOpenAsIcon: false,
  btnOpenIcon: '',

  children: null,
  bodyContainerClassName: 'modal-body-container',
  bodyContainerStyle: {},

  btnContainerClassName: 'btn-group float-right',
  btnContainerStyle: {},

  wrapperClassName: null,

  btnSaveText: 'react.default.button.save.label',
  btnSaveDefaultText: 'Save',
  btnSaveClassName: 'btn btn-outline-success',
  btnSaveStyle: {},
  btnSaveDisabled: false,

  btnCancelText: 'react.default.button.cancel.label',
  btnCancelDefaultText: 'Cancel',
  btnCancelClassName: 'btn btn-outline-secondary',
  btnCancelStyle: {},

  renderButton: undefined,

  onOpen: () => null,
  onSave: () => null,
  onClose: () => null,

  validate: () => null,
  renderBodyWithValues: () => null,
  initialValues: [],
  fields: {},
  formProps: {},
  showModal: false,
};
