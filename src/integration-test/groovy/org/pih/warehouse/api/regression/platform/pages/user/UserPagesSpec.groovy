package org.pih.warehouse.api.regression.platform.pages.user

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The user administration server pages (UserController): creating, editing, activating and deleting users, their
 * photos and location roles, as the admin user.
 */
class UserPagesSpec extends RegressionApiSpec {

    static final String PASSWORD = 'Page-passw0rd!'

    ServerPages pages

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    /** Creates a user through the create form; returns its id. */
    String createUser() {
        String username = randomUtil.randomStringFieldValue('pageuser').toLowerCase()
        Response saved = pages.post('/user/save', [username: username, firstName: 'Page', lastName: 'User',
                email: "${username}@example.com", password: PASSWORD, passwordConfirm: PASSWORD])
        String id = User.withNewSession { User.findByUsername(username)?.id }
        assert id, "user not created: ${saved.statusCode}"
        assert redirectPath(saved) == "/user/edit/${id}"
        return id
    }

    def <T> T withUser(String id, Closure<T> closure) {
        return User.withNewSession { closure(User.get(id)) }
    }

    void 'the user list and forms'() {
        given:
        String id = createUser()
        String username = withUser(id) { it.username }

        expect:
        page(pages.get('/user/list', [q: username])).contains(username)
        page(pages.get('/user/create')) != null
        redirectPath(pages.get('/user/index')) == '/user/list'
        redirectPath(pages.get("/user/redirect/${id}")) == "/user/edit/${id}"
        page(pages.get("/user/show/${id}")).contains(username)
        page(pages.get("/user/edit/${id}")).contains(username)
        page(pages.get("/user/changePhoto/${id}")) != null
        page(pages.get("/user/cropPhoto/${id}")) != null
        ['show', 'edit', 'changePhoto', 'cropPhoto'].every {
            redirectPath(pages.get("/user/${it}/unknown-user")) == '/user/list'
        }
    }

    void 'a new user starts inactive and the form refuses mismatched passwords'() {
        when:
        String id = createUser()

        then:
        !withUser(id) { it.active }

        when:
        String refused = page(pages.post('/user/save', [username: 'mismatched', firstName: 'Page', lastName: 'User',
                password: PASSWORD, passwordConfirm: 'something else']))

        then:
        refused != null
        User.withNewSession { User.findByUsername('mismatched') } == null
    }

    void 'a photo that is not an image is refused'() {
        given:
        String id = createUser()

        when:
        String refused = page(pages.request().multiPart('photo', 'notes.txt', 'text'.bytes, 'text/plain')
                .post("/user/uploadPhoto/${id}"))

        then:
        refused != null
        withUser(id) { it.photo } == null
    }

    void 'a new user is created with the roles chosen on the form'() {
        given:
        String roleId = Role.withNewSession { Role.findByRoleType(RoleType.ROLE_BROWSER).id }
        String username = randomUtil.randomStringFieldValue('roleuser').toLowerCase()

        when:
        Response saved = pages.post('/user/save', [username: username, firstName: 'Role', lastName: 'User',
                email: "${username}@example.com", password: PASSWORD, passwordConfirm: PASSWORD, roles: [roleId]])
        String id = User.withNewSession { User.findByUsername(username)?.id }

        then:
        redirectPath(saved) == "/user/edit/${id}"
        withUser(id) { it.roles*.id } == [roleId]
    }

    @PendingFeature(reason = 'uploading a user photo answers 500')
    void 'a photo is uploaded for a user'() {
        given:
        String id = createUser()
        byte[] png = Base64.decoder.decode(
                'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==')

        when:
        Response uploaded = pages.request().multiPart('photo', 'photo.png', png, 'image/png')
                .post("/user/uploadPhoto/${id}")

        then:
        redirectPath(uploaded) == "/user/edit/${id}"
        withUser(id) { it.photo } == png
    }

}
