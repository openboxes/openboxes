import grails.util.BuildSettings

/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

target('verifyClasspath': '''Check the classpath for duplicate classes''') {
    BuildSettings buildSettings = grailsSettings

    def dependencies = buildSettings.testDependencies

    String jarsDir = "${buildSettings.projectTargetDir}/jartemp"

    ant.delete(dir:jarsDir)
    ant.mkdir(dir:jarsDir)

    dependencies.each { File jar ->
        ant.copy(todir: jarsDir, file: jar.absolutePath, verbose: true )
    }

    ant.taskdef(name:"report", classname:"org.jboss.tattletale.ant.ReportTask")

    String reportDir = "${buildSettings.projectTargetDir}/tattletale-report"

    ant.delete(dir:reportDir)
    ant.mkdir(dir:reportDir)

    ant.report(source: jarsDir, destination: reportDir, reports: "multiplejars")

}

setDefaultTarget("verifyClasspath")
