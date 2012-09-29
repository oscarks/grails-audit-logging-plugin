import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogListener
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogListenerUtil
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners

/**
 * @author Shawn Hartsock
 * <p/>
 * Credit is due to the following other projects,
 * first is Kevin Burke's HibernateEventsGrailsPlugin
 * second is the AuditLogging post by Rob Monie at
 * http://www.hibernate.org/318.html
 * <p/>
 * I've combined the two sources to create a Grails
 * Audit Logging plugin that will track individual
 * changes to columns.
 * <p/>
 * See Documentation:
 * http://grails.org/plugin/audit-logging
 * <p/>
 * Changes:
 * <p/>
 * Release 0.3
 * <p/>
 *      * actorKey and username features allow for the logging of
 *        user or userPrincipal for most security systems.
 * <p/>
 *
 * Release 0.4
 * <p/>
 * 		* custom serializable implementation for AuditLogEvent so events can happen
 *        inside a webflow context.
 * <p/>
 *      * tweak application.properties for loading in other grails versions
 * <p/>
 *      * update to views to show URI in an event
 * <p/>
 *      * fix missing oldState bug in change event
 * <p/>
 *
 * Release 0.4.1
 * <p/>
 *      * repackaged for Grails 1.1.1 see GRAILSPLUGINS-1181
 * <p/>
 *
 * Release 0.5_ALPHA see GRAILSPLUGINS-391
 * <p/>
 *      * changes to AuditLogEvent domain object uses composite id to simplify logging
 * <p/>
 *      * changes to AuditLogListener uses new domain model with separate transaction
 *        for logging action to avoid invalidating the main hibernate session.
 * <p/>
 * Release 0.5_BETA see GRAILSPLUGINS-391
 * <p/>
 *      * testing version released generally.
 * <p/>
 * Release 0.5 see GRAILSPLUGINS-391, GRAILSPLUGINS-1496, GRAILSPLUGINS-1181, GRAILSPLUGINS-1515, GRAILSPLUGINS-1811
 * <p/>
 * Release 0.5.1 fixes regression in field logging
 * <p/>
 * Release 0.5.2 see GRAILSPLUGINS-1887 and GRAILSPLUGINS-1354
 * <p/>
 * Release 0.5.3 GRAILSPLUGINS-2135 GRAILSPLUGINS-2060 &amp; an issue with extra JAR files that are somehow getting released as part of the plugin
 * <p/>
 * Release 0.5.4 compatibility issues with Grails 1.3.x
 * <p/>
 * Release 0.5.4.1 compatibility issues with Grails 2.1.x - updated by Andréas Kühne <andreas@kuhne.se>
 * <p/>
 */
class AuditLoggingGrailsPlugin {
    def version = "0.5.4.1"
    def grailsVersion = '2.1 > *'
    def author = "Shawn Hartsock"
    def authorEmail = "hartsock@acm.org"
    def title = "adds auditable to GORM domain classes"
    def description = """ Automatically log change events for domain objects.
The Audit Logging plugin adds an instance hook to domain objects that allows you to hang
Audit events off of them. The events include onSave, onUpdate, onChange, onDelete and
when called the event handlers have access to oldObj and newObj definitions that
will allow you to take action on what has changed.

Stable Releases:
    0.5.3 (Grails 1.2 or below)
    0.5.4 (Grails 1.3 or above)
	0.5.4.1 (Grails 2.1 or above)
    1.0.0 (Grails 2.0 or above)
    """
    def dependsOn = [:]
    def loadAfter = ['core', 'hibernate']

    def doWithSpring = {
		if (manager?.hasGrailsPlugin("hibernate")) {
			auditLogListener(AuditLogListener) {
				verbose = application.config?.auditLog?.verbose ?: false
				transactional = application.config?.auditLog?.transactional ?: false
				sessionAttribute = application.config?.auditLog?.sessionAttribute ?: ""
				actorKey = application.config?.auditLog?.actorKey ?: ""
			}

			//PreDeleteEventListener, PostInsertEventListener, PostUpdateEventListener
			hibernateEventListeners(HibernateEventListeners) {
				listenerMap = [
						'post-insert': auditLogListener,
						'post-update': auditLogListener,
						'pre-delete': auditLogListener]
			}
		}
    }

    def doWithApplicationContext = { applicationContext ->
        // pulls in the bean to inject and init
        AuditLogListener listener = applicationContext.getBean("auditLogListener")
        // allows user to over-ride the maximum length the value stored by the audit logger.
        listener.setActorClosure(application.config?.auditLog?.actorClosure ?: AuditLogListenerUtil.actorDefaultGetter)
        listener.init()
        if (application.config?.auditLog?.TRUNCATE_LENGTH) {
            listener.truncateLength = new Long(application.config?.auditLog?.TRUNCATE_LENGTH)
        }
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }


    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
