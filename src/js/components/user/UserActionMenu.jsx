import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { showUserActions, hideUserActions } from '../../actions';


class UserActionMenu extends Component {
  constructor(props) {
    super(props);

    this.toggleUserActionMenu = this.toggleUserActionMenu.bind(this);
  }

  toggleUserActionMenu() {
    if (this.props.userActionMenuOpen) {
      this.props.hideUserActions();
    } else {
      this.props.showUserActions();
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
          {`${this.props.currentUser.username} [${this.props.highestRole}]`}
        </button>

        <ul
          className="dropdown"
          hidden={!this.props.userActionMenuOpen}
        >
          {this.props.menuItems.map(item => (
            <li key={item.label}>
              <a href={item.linkAction}> <img alt={item.label} src={item.linkIcon} />{item.label}
              </a>
            </li>))}
        </ul>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  userActionMenuOpen: state.session.userActionMenuOpen,
  menuItems: state.session.menuItems,
  currentUser: state.session.user,
  highestRole: state.session.highestRole,
});

export default connect(mapStateToProps, {
  showUserActions,
  hideUserActions,
})(UserActionMenu);

UserActionMenu.propTypes = {
  // List of action items for the user action menu
  menuItems: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    linkIcon: PropTypes.string.isRequired,
    linkAction: PropTypes.string.isRequired,
  }).isRequired).isRequired,
  // Current user
  currentUser: PropTypes.shape({
    username: PropTypes.string.isRequired,
  }).isRequired,
  // Boolean to show modal or not
  userActionMenuOpen: PropTypes.bool.isRequired,
  // Function to show the location modal
  showUserActions: PropTypes.func.isRequired,
  // Function to hide the location modal
  hideUserActions: PropTypes.func.isRequired,
  // Highest role of the user for this location
  highestRole: PropTypes.string.isRequired,
};
