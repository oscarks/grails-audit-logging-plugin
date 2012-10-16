package org.codehaus.groovy.grails.plugins.orm.auditable

import org.joda.time.DateTime
import org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone

/**
 * AuditLogEvents are reported to the AuditLog table
 * this requires you to set up a table or allow
 * Grails to create a table for you.
 */
class AuditLogEvent implements java.io.Serializable {
  private static final long serialVersionUID = 1L

  static auditable = false

  DateTime dateCreated
  DateTime lastUpdated

  String actor
  String uri
  String className
  String persistedObjectId
  Long persistedObjectVersion = 0

  String eventName
  String propertyName
  String oldValue
  String newValue

  static constraints = {
    actor(nullable:true)
    uri(nullable:true)
    className(nullable:true)
    persistedObjectId(nullable:true)
    persistedObjectVersion(nullable:true)
    eventName(nullable:true)
    propertyName(nullable:true)
    oldValue(nullable:true, maxSize: 65535)
    newValue(nullable:true, maxSize: 65535)
  }

  static mapping = {
    table 'audit_log'
    cache usage:'read-only', include:'non-lazy'
    version false
	dateCreated type: PersistentDateTimeWithZone, {
		column name: "date_created_timestamp"
		column name: "date_created_zone"
	}
	lastUpdated type: PersistentDateTimeWithZone, {
		column name: "last_updated_timestamp"
		column name: "last_updated_zone"
	}
  }

  /**
   * A very Groovy de-serializer that maps a stored map onto the object
   * assuming that the keys match attribute properties.
   */
  private void readObject(java.io.ObjectInputStream input) throws IOException, ClassNotFoundException {
    def map = (java.util.LinkedHashMap) input.readObject()
    map.each({ k,v -> this."$k" = v })
  }

  /**
   * Because Closures do not serialize we can't send the constraints closure
   * to the Serialize API so we have to have a custom serializer to allow for
   * this object to show up inside a webFlow context.
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    def map = [
            id:id,
            dateCreated:dateCreated,
            lastUpdated:lastUpdated,

            actor:actor,
            uri:uri,
            className:className,
            persistedObjectId:persistedObjectId,
            persistedObjectVersion:persistedObjectVersion,

            eventName:eventName,
            propertyName:propertyName,
            oldValue:oldValue,
            newValue:newValue,
    ]
    out.writeObject(map)
  }

  String toString() {
    "audit log ${dateCreated} ${actor?'user ' + actor:'user ?'} " + 
        "${eventName} ${className} " + 
        "id:${persistedObjectId} version:${persistedObjectVersion}"
  }
}

