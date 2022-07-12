package org.pih.warehouse.auth

import org.pih.warehouse.core.User

import org.springframework.context.ApplicationEvent

class UserSignupEvent extends ApplicationEvent {

    Map additionalQuestions

    UserSignupEvent(User user, Map additionalQuestions) {
        super(user)
        this.additionalQuestions = additionalQuestions
    }
}
