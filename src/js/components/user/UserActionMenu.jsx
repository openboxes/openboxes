import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { showModal, hideModal } from '../../actions';


class UserActionMenu extends Component {
  constructor(props) {
    super(props);

    this.toggleUserActionMenu = this.toggleUserActionMenu.bind(this);
  }

  toggleUserActionMenu() {
    if (this.props.userActionMenuOpen) {
      this.props.hideModal('userActionMenu');
    } else {
      this.props.showModal('userActionMenu');
    }
  }


  render() {
    return (
      <div
        className="user-action-menu"
      >
        <button
          type="button"
          className="btn btn-light ml-1"
          onClick={this.toggleUserActionMenu}
        >
          {`${this.props.currentUser.username} - [${this.props.highestRole}]`}
        </button>

        <ul
          className="dropdown"
          hidden={!this.props.userActionMenuOpen}
        >
          {this.props.actionMenuItems.map(item => (
            <li key={item.label}>
              <a href={`${item.linkAction}/${this.props.currentUser.id}`}> <img alt={item.label} src={item.linkIcon} />{item.label}
              </a>
            </li>))}
        </ul>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  userActionMenuOpen: state.session.userActionMenuOpen,
  actionMenuItems: state.session.actionMenuItems,
  currentUser: state.session.user,
  highestRole: state.session.highestRole,
});

export default connect(mapStateToProps, {
  showModal,
  hideModal,
})(UserActionMenu);

UserActionMenu.propTypes = {
  // List of action items for the user action menu
  actionMenuItems: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    linkIcon: PropTypes.string.isRequired,
    linkAction: PropTypes.string.isRequired,
  }).isRequired).isRequired,
  // Current user
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    username: PropTypes.string.isRequired,
  }).isRequired,
  // Boolean to show modal or not
  userActionMenuOpen: PropTypes.bool.isRequired,
  // Function to show the location modal
  showModal: PropTypes.func.isRequired,
  // Function to hide the location modal
  hideModal: PropTypes.func.isRequired,
  // Highest role of the user for this location
  highestRole: PropTypes.string.isRequired,
};
