import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Modal from 'react-modal';

Modal.setAppElement('#root');

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

  save() {
    this.props.onSave();
    this.setState({ showModal: false });
  }

  render() {
    return (
      <div>
        <button
          className={this.props.btnOpenClassName}
          style={this.props.btnOpenStyle}
          onClick={() => this.openModal()}
        >
          {this.props.btnOpenText}
        </button>
        <Modal
          isOpen={this.state.showModal}
          onRequestClose={this.closeModal}
          className="modal-content-custom"
          shouldCloseOnOverlayClick={false}
        >
          <h5 className="text-center">{this.props.title}</h5>
          <hr />

          <div className={this.props.bodyContainerClassName} style={this.props.bodyContainerStyle}>
            {this.props.children}
          </div>

          <hr />
          <div
            className={this.props.btnContainerClassName}
            role="group"
            style={this.props.btnContainerStyle}
          >
            <button
              type="button"
              className={this.props.btnSaveClassName}
              style={this.props.btnSaveStyle}
              onClick={() => this.save()}
            >
              {this.props.btnSaveText}
            </button>
            <button
              type="button"
              className={this.props.btnCancelClassName}
              style={this.props.btnCancelStyle}
              onClick={() => this.closeModal()}
            >
              {this.props.btnCancelText}
            </button>
          </div>
        </Modal>
      </div>
    );
  }
}

export default ModalWrapper;

ModalWrapper.propTypes = {
  /* Open button properties */
  btnOpenText: PropTypes.string,
  btnOpenClassName: PropTypes.string,
  btnOpenStyle: PropTypes.shape({}),

  /* Modal title property */
  title: PropTypes.string.isRequired,

  /* Modal body container properties */
  children: PropTypes.element.isRequired,
  bodyContainerClassName: PropTypes.string,
  bodyContainerStyle: PropTypes.shape({}),

  /* Button container properties */
  btnContainerClassName: PropTypes.string,
  btnContainerStyle: PropTypes.shape({}),

  /* Save button properties */
  btnSaveText: PropTypes.string,
  btnSaveClassName: PropTypes.string,
  btnSaveStyle: PropTypes.shape({}),

  /* Cancel button properties */
  btnCancelText: PropTypes.string,
  btnCancelClassName: PropTypes.string,
  btnCancelStyle: PropTypes.shape({}),

  /* Functional properties */
  onOpen: PropTypes.func,
  onSave: PropTypes.func,
  onClose: PropTypes.func,
};

ModalWrapper.defaultProps = {
  btnOpenText: 'Open',
  btnOpenClassName: 'btn btn-outline-primary',
  btnOpenStyle: {},

  bodyContainerClassName: 'modal-body-container',
  bodyContainerStyle: {},

  btnContainerClassName: 'btn-group float-right',
  btnContainerStyle: {},

  btnSaveText: 'Save',
  btnSaveClassName: 'btn btn-outline-success',
  btnSaveStyle: {},

  btnCancelText: 'Cancel',
  btnCancelClassName: 'btn btn-outline-secondary',
  btnCancelStyle: {},

  onOpen: () => null,
  onSave: () => null,
  onClose: () => null,
};
