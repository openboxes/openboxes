package testutils

import grails.test.ControllerUnitTestCase
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

@Category(ControllerUnitTestCase)
class MockBindDataMixin {
    void mockBindData() {
        def mc = controller.metaClass
        def bind = new BindDynamicMethod()
        mc.bindData = { Object target, Object args ->
            bind.invoke(controller, "bindData", [target, args] as Object[])
        }
        mc.bindData = { Object target, Object args, List disallowed ->
            bind.invoke(controller, "bindData", [target, args, [exclude: disallowed]] as Object[])
        }
        mc.bindData = { Object target, Object args, List disallowed, String filter ->
            bind.invoke(controller, "bindData", [target, args, [exclude: disallowed], filter] as Object[])
        }
        mc.bindData = { Object target, Object args, Map includeExclude ->
            bind.invoke(controller, "bindData", [target, args, includeExclude] as Object[])
        }
        mc.bindData = { Object target, Object args, Map includeExclude, String filter ->
            bind.invoke(controller, "bindData", [target, args, includeExclude, filter] as Object[])
        }
        mc.bindData = { Object target, Object args, String filter ->
            bind.invoke(controller, "bindData", [target, args, filter] as Object[])
        }
    }
}
