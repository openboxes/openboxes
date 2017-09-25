import React from 'react';

function Todo(props) {

    const todo = props.todo;

    return(
        <li>
            <input id={ todo.id } type="checkbox"
                   checked={ todo.complete ? 'checked' : '' }
                   onChange={ props.toggleComplete } />
            <span>{ todo.name }</span>
        </li>
    );
}