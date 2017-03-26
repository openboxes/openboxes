import org.codehaus.groovy.grails.plugins.GrailsPlugin

class WatchScaffoldingGrailsPlugin {
  def version = "0.1"
  def grailsVersion = "2.0 > *"
  def dependsOn = [:]
  def pluginExcludes = [ "grails-app/views/error.gsp" ]

  def title = "Watch Scaffolding Plugin"
  def author = "Your name"
  def authorEmail = ""
  def description = '''\
Watches for changes to scaffolding templates and reloads dynamically-scaffolded
controllers and views.
'''
  // URL to the plugin's documentation
  def documentation = "http://grails.org/plugin/watch-scaffolding"

  // watch for changes to scaffolding templates...
  def watchedResources = "file:./src/templates/scaffolding/*"

  // ... and kick the scaffolding plugin when they change
  def onChange = { event ->
    event.manager.getGrailsPlugin('scaffolding').notifyOfEvent(
            GrailsPlugin.EVENT_ON_CHANGE, null)
  }

  // rest of plugin options are no-op
  def onConfigChange = { event -> }
  def doWithWebDescriptor = { xml -> }
  def doWithSpring = { }
  def doWithDynamicMethods = { ctx -> }
  def doWithApplicationContext = { applicationContext -> }
  def onShutdown = { event -> }
}