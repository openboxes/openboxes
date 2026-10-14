package org.pih.warehouse.regression.platform.core


import org.pih.warehouse.core.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.mock.web.MockMultipartFile

class PersonControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<PersonController>, DataTest {
    Class getDomainClass() { Person }
    List<Class> getMockedDomains() { [Person, User] }
    String getInstanceKey() { "personInstance" }
    List<String> getListKeys() { ["personInstanceList", "personInstanceTotal"] }
    Map getValidParams() { [firstName: "Ana", lastName: "Silva", email: "ana@example.org"] }
    Map getInvalidParams() { [firstName: "", lastName: "", email: "not an email"] }
    def createInstance() { new Person(firstName: "Bruno", lastName: "Costa").save(failOnError: true) }
    String saveRedirect(def id) { "/person/list/${id}" }
    String updateRedirect(def id) { "/person/list/${id}" }

    void "list searches people through the user service"() {
        given:
        Person bruno = createInstance()
        controller.userService = Mock(UserService)

        when:
        params.q = "bru"
        def model = controller.list()

        then:
        1 * controller.userService.findPersons(["%bru%"] as String[], _) >> new grails.gorm.PagedResultList(null).with {
            resultList = [bruno]
            totalCount = 1
            it
        }
        model.personInstanceList == [bruno]
        model.personInstanceTotal == 1
    }

    void "redirect shows the person"() {
        when:
        params.id = "p1"
        controller.redirect()

        then:
        response.redirectedUrl == "/person/show/p1"
    }

    void "people can be converted to users and back"() {
        given:
        controller.userService = Mock(UserService)
        params.id = "p1"

        when:
        controller.convertPersonToUser()

        then:
        1 * controller.userService.convertPersonToUser("p1")
        response.redirectedUrl == "/user/edit/p1"

        when:
        response.reset()
        controller.convertUserToPerson()

        then:
        1 * controller.userService.convertUserToPerson("p1")
        response.redirectedUrl == "/person/show/p1"
    }
}

class LocalizationControllerSpec extends CrudControllerSpecSupport implements ControllerUnitTest<LocalizationController>, DataTest {
    Class getDomainClass() { Localization }
    List<Class> getMockedDomains() { [Localization, User] }
    String getInstanceKey() { "localizationInstance" }
    List<String> getListKeys() { null }
    Map getValidParams() { [code: "app.title", locale: "fr", text: "Titre"] }
    Map getInvalidParams() { [code: null] }
    String getCreatedMessage() { "default.saved.message" }
    def createInstance() { new Localization(code: "app.name", locale: "en", text: "OpenBoxes").save(failOnError: true) }
    String saveRedirect(def id) { "/localization/list/${id}" }
    String updateRedirect(def id) { "/localization/list/${id}" }

    void "list shows the translations of the user's language matching the search"() {
        given:
        config.openboxes.locale.defaultLocale = "en"
        session.user = new User(username: "translator", locale: Locale.FRENCH)
        createInstance()
        new Localization(code: "app.title", locale: "fr", text: "Titre").save(failOnError: true)
        new Localization(code: "app.subtitle", locale: "fr", text: "Sous-titre").save(failOnError: true)

        when:
        params.q = "app.t"
        def model = controller.list()

        then:
        params.locale == "fr"
        model.localizationInstanceList*.code == ["app.title"]
        model.localizationInstanceTotal == 1
    }

    void "save updates an existing translation and refuses stale versions"() {
        given:
        Localization existing = createInstance()
        request.method = "POST"

        when:
        params.id = existing.id
        params.text = "OpenBoxes WMS"
        controller.save()

        then:
        existing.text == "OpenBoxes WMS"
        response.redirectedUrl == "/localization/list/${existing.id}"

        when:
        response.reset()
        existing.version = 5
        params.version = "1"
        controller.save()

        then:
        view == "/localization/edit"
    }

    void "export writes the translations of a locale as a properties file"() {
        given:
        new Localization(code: "b.key", locale: "fr", text: " Bonjour ").save(failOnError: true)
        new Localization(code: "a.key", locale: "fr", text: "Salut").save(failOnError: true)
        createInstance()
        session.user = new User(username: "translator", locale: Locale.ENGLISH)

        when:
        params.locale = "fr"
        controller.export()
        List<String> lines = response.text.readLines()

        then:
        response.getHeader("Content-disposition") == 'attachment; filename="messages_fr.properties"'
        lines[0] == "# messages_fr.properties for French"
        lines[2] == "a.key = Salut"
        lines[3] == "b.key = Bonjour"
        lines.size() == 4
    }

    void "upload imports a properties file into the given locale"() {
        given:
        new Localization(code: "app.title", locale: "fr", text: "Old").save(failOnError: true)
        request.method = "POST"
        LocalizationCommand command = new LocalizationCommand(locale: Locale.FRENCH)
        command.messageProperties = new MockMultipartFile("messageProperties", "messages_fr.properties", "text/plain",
                "app.title=Titre\napp.new=Nouveau\n".getBytes("UTF-8"))

        when:
        controller.upload(command)

        then:
        Localization.findByCode("app.title").text == "Titre"
        Localization.findByCode("app.new").text == "Nouveau"
        Localization.findByCode("app.new").locale == "fr"
        flash.message.startsWith("default.uploaded.message")
        response.redirectedUrl == "/localization/list"
    }
}
