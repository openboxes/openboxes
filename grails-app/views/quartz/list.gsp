%{--\
This is a copy of quartz/list template from the quartz-monitor-1.3
Copied to modify it to link to the jobs/show action
--}%

<%@ page import="org.quartz.Trigger" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<g:set var="layoutName" value="${grailsApplication.config.quartz?.monitor?.layout}" />
	<meta name="layout" content="${layoutName ?: 'main'}" />
	<title>Quartz Jobs</title>
	<asset:javascript src="quartz-monitor.js"/>
	<asset:stylesheet src="quartz-monitor.css"/>
	<g:if test="${grailsApplication.config.getProperty("quartz.monitor.showCountdown", Boolean, true)}">
		<asset:javascript src="jquery.countdown.js" />
		<asset:javascript src="jquery.color.js" />
		<asset:stylesheet src="jquery.countdown.css"/>
	</g:if>
	<g:if test="${grailsApplication.config.getProperty("quartz.monitor.showTickingClock", Boolean, true)}">
		<asset:javascript src="jquery.clock.js" />
		<asset:stylesheet src="jquery.clock.css" />
	</g:if>
</head>
<body>
<div class="nav">
	<span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
</div>
<div class="body">
	<h1 id="quartz-title">
		Quartz Jobs
		<g:if test="${schedulerInStandbyMode}">
			<a href="<g:createLink action="startScheduler"/>"><asset:image class="quartz-tooltip" data-tooltip="Start scheduler" src="play-all.png" /></a>
		</g:if>
		<g:else>
			<a href="<g:createLink action="stopScheduler"/>"><asset:image class="quartz-tooltip" data-tooltip="Pause scheduler" src="pause-all.png" /></a>
		</g:else>
	</h1>
	<g:if test="${flash.message}">
		<div class="message" role="status" aria-label="message">${flash.message}</div>
	</g:if>
	<div id="clock" data-time="${now.time}">
		<h3>Current Time: ${now}</h3>
	</div>
	<div class="list">
		<table id="quartz-jobs">
			<thead>
			<tr>
				<th>Name</th>
				<g:if test="${grailsApplication.config.quartz.monitor.showTriggerNames}">
					<th>Trigger Name</th>
				</g:if>
				<th>Last Run</th>
				<th class="quartz-to-hide">Result</th>
				<th>Next Scheduled Run</th>
				<th>Actions</th>
			</tr>
			</thead>
			<tbody>
			<g:each in="${jobs}" status="i" var="job">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td><g:link controller="jobs" action="show" id="${job.name}">${job.name}</g:link></td>
					<g:if test="${grailsApplication.config.quartz.monitor.showTriggerNames}">
						<td>${job.trigger?.name}</td>
					</g:if>
					<g:set var="tooltip">${(job.error ? "Job threw exception: " + job.error + ". " : "") + (job.duration >= 0 ? "Job ran in: " + job.duration + "ms" : "")}</g:set>
					<td class="quartz-tooltip quartz-status ${job.status?:"not-run"}" data-tooltip="${tooltip}">${job.lastRun}</td>
					<td class="quartz-to-hide">${tooltip}</td>
					<g:if test="${schedulerInStandbyMode || job.triggerStatus == Trigger.TriggerState.PAUSED}">
						<td class="hasCountdown countdown_amount">Paused</td>
					</g:if>
					<g:else>
						<td class="quartz-countdown" data-next-run="${job.trigger?.nextFireTime?.time ?: ""}">${job.trigger?.nextFireTime}</td>
					</g:else>
					<td class="quartz-actions">
						<g:if test="${job.status != 'running'}">
							<g:if test="${job.trigger}">
								<a href="<g:createLink action="stop" params="[jobName:job.name, triggerName:job.trigger.name, triggerGroup:job.trigger.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Stop job from running again" src="stop.png"/></a>
								<g:if test="${job.triggerStatus == Trigger.TriggerState.PAUSED}">
									<a href="<g:createLink action="resume" params="[jobName:job.name, jobGroup:job.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Resume job schedule" src="resume.png"/></a>
								</g:if>
								<g:elseif test="${job.trigger.mayFireAgain()}">
									<a href="<g:createLink action="pause" params="[jobName:job.name, jobGroup:job.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Pause job schedule" src="pause.png"/></a>
								</g:elseif>
							</g:if>
							<g:else>
								<a href="<g:createLink action="start" params="[jobName:job.name, jobGroup:job.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Start job schedule" src="start.png"/></a>
							</g:else>
							<a href="<g:createLink action="runNow" params="[jobName:job.name, jobGroup:job.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Run now" src="run.png"/></a>
							<g:if test="${job.trigger instanceof org.quartz.CronTrigger}">
								<a href="<g:createLink action="editCronTrigger" params="[triggerName:job.trigger.name, triggerGroup:job.trigger.group]"/>"><asset:image class="quartz-tooltip" data-tooltip="Reschedule" src="reschedule.png"/></a>
							</g:if>
						</g:if>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</div>
</body>
</html>
