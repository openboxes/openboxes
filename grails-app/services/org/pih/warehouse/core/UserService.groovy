/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import com.unboundid.ldap.sdk.BindRequest
import com.unboundid.ldap.sdk.BindResult
import com.unboundid.ldap.sdk.DN
import com.unboundid.ldap.sdk.DNEntrySource
import com.unboundid.ldap.sdk.DereferencePolicy
import com.unboundid.ldap.sdk.Entry
import com.unboundid.ldap.sdk.Filter
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.ResultCode
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.SimpleBindRequest
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.collections.ListUtils
import org.pih.warehouse.auth.AuthService
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.MatchMode
import org.hibernate.criterion.Order
import org.hibernate.transform.Transformers

import util.StringUtil

import javax.net.SocketFactory
import java.security.GeneralSecurityException

class UserService {

    def dataSource
    def grailsApplication
    SessionFactory sessionFactory
    boolean transactional = true

    User getUser(String id) {
        return User.get(id)
    }


    def updateUser(String userId, String currentUserId, Map params) {

        def userInstance = User.get(userId)

        // Password in the db is different from the one specified
        // so the user must have changed the password.  We need
        // to compare the password with confirm password before
        // setting the new password in the database
        if (params.changePassword && userInstance.password != params.password) {
            userInstance.properties = params
            userInstance.password = params?.password?.encodeAsPassword();
            userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword();
        } else {
            userInstance.properties = params
            // Needed to bypass the password == passwordConfirm validation
            userInstance.passwordConfirm = userInstance.password
        }
        // If a non-admin user edits their profile they will not have access to
        // the roles or location roles, so we need to prevent the updateRoles
        // method from being called.
        if (params.locationRolePairs) {
            updateRoles(userInstance, params.locationRolePairs)
        }

        // We need to cache current role and check edit privilege here because the roles association
        // may change once we merge user and request parameters
        def currentUser = User.load(currentUserId)
        def canEditRoles = canEditUserRoles(currentUser, userInstance)

        // Check to make sure the roles are dirty
        def currentRoles = new HashSet(userInstance?.roles)
        def updatedRoles = Role.findAllByIdInList(params.list("roles"))
        def isRolesDirty = !ListUtils.isEqualList(updatedRoles, currentRoles) && params.updateRoles
        log.info "User update: ${updatedRoles} vs ${currentRoles}: isDirty=${isRolesDirty}, canEditRoles=${canEditRoles}"
        if (isRolesDirty && !canEditRoles) {
            Object [] args = [currentUser.username, userInstance.username]
            userInstance.errors.rejectValue("roles", "user.errors.cannotEditUserRoles.message", args, "User cannot edit user roles")
            throw new ValidationException("user.errors.cannotEditUserRoles.message", userInstance.errors)
        }

        log.info "User has errors: ${userInstance.hasErrors()} ${userInstance.errors}"
        return userInstance.save(failOnError: true)
    }

    private void updateRoles(user, locationRolePairs){
        def newAndUpdatedRoles = locationRolePairs.keySet().collect{ locationId ->
            if(locationRolePairs[locationId]){
                def location = Location.get(locationId)
                def role = Role.get(locationRolePairs[locationId])
                def existingRole = user.locationRoles.find{it.location == location}
                if(existingRole){
                    existingRole.role = role
                }else{
                    def newLocationRole = new LocationRole(user: user, location:location, role: role)
                    user.addToLocationRoles(newLocationRole)
                }
            }
        }
        def rolesToRemove = user.locationRoles.findAll{ oldRole ->
            !locationRolePairs[oldRole.location.id]
        }
        rolesToRemove.each{
            user.removeFromLocationRoles(it)
        }
    }

    Boolean isSuperuser(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean isUserAdmin(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean isUserManager(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean canUserBrowse(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_BROWSER, RoleType.ROLE_ASSISTANT]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean hasRoleFinance(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_FINANCE]
            return getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
        }
        return false;
    }


    Boolean canEditUserRoles(User currentUser, User otherUser) {
        def location = AuthService.currentLocation.get()
        return isSuperuser(currentUser) || (currentUser.getHighestRole(location) >= otherUser.getHighestRole(location))
    }


    Boolean isUserInRole(String userId, Collection roleTypes) {
        Collection acceptedRoleTypes = RoleType.expand(roleTypes)
        User user = getUser(userId)
        return getEffectiveRoles(user).any { Role role ->
            boolean acceptedRoleType = acceptedRoleTypes.contains(role.roleType)
            log.info "${role.name} ${role.roleType} = ${acceptedRoleType}"
            return acceptedRoleType
        }
    }

    boolean hasRoleFinance() {
        User user = AuthService.currentUser.get()
        return hasRoleFinance(user)
    }

    void assertCurrentUserHasRoleFinance() {
        User user = AuthService.currentUser.get()
        if (!hasRoleFinance(user)) {
            throw new IllegalStateException("User ${user.username} must have ROLE_FINANCE role")
        }
    }

    def findPersons(String [] fields, String [] terms) {
        return findPersons(fields, terms, [:])
    }

    def findPersons(String [] fields, String[] terms, params) {
        def session = sessionFactory.currentSession
        def criteria = session.createCriteria(Person.class)

        if (fields) {
            def projection = Projections.projectionList()
            fields.each {
                projection.add(Projections.property(it), it)
            }
            criteria.setProjection(projection)
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        }

        if(terms) {
            terms.each { term ->
                Criterion lastName = Restrictions.ilike("this.lastName", "%" + term + "%", MatchMode.ANYWHERE)
                Criterion firstName = Restrictions.ilike("this.firstName", "%" + term + "%", MatchMode.ANYWHERE)
                Criterion email = Restrictions.ilike("this.email", "%" + term + "%", MatchMode.ANYWHERE)
                criteria.add(Restrictions.or(Restrictions.or(lastName, firstName), email))
            }
        }
        criteria.addOrder(Order.desc("lastName"))

        return criteria.list()
    }

    def findUsers(String query, Map params) {
        println "findUsers: " + query + " : " + params
        def criteria = User.createCriteria()
        def results = criteria.list (params) {
            if (query) {
                or {
                    like("firstName", query)
                    like("lastName", query)
                    like("email", query)
                    like("username", query)
                }
            }
            if (params.status) {
                eq("active", Boolean.valueOf(params.status))
            }
            // Disabled to allow the user to choose sorting mechanism (should probably add this as the default)
            //order("lastName", "desc")
        }

        return results;
    }


    void convertPersonToUser(String personId) {
        def user = User.get(personId)
        if (!user) {
            def person = Person.get(personId)
            if (person) {
                def encodedPassword = "password"?.encodeAsPassword()
                Sql sql = new Sql(dataSource)
                sql.execute('insert into user (id, username, password) values (?, ?, ?)', [person?.id, person?.email, encodedPassword])
            }
        }
    }

    void convertUserToPerson(String personId) {
        def person = Person.get(personId)
        if (person) {
            Sql sql = new Sql(dataSource)
            sql.execute('delete from user where id = ?', [personId])
        }
    }

    def findUsersByRoleType(RoleType roleType) {
        def users = []
        def role = Role.findByRoleType(roleType)
        if (role) {
            def criteria = User.createCriteria()
            users = criteria.list {
                eq("active", true)
                roles {
                    eq("id", role.id)
                }
            }
        }
        return users;
    }

    private def rolesForCurrentLocation(User user){
        def currentLocation = AuthService.currentLocation?.get()
        return user.getRolesByCurrentLocation(currentLocation)
    }

    private def getEffectiveRoles(User user){
        def currentLocation = AuthService.currentLocation?.get()
        return user.getEffectiveRoles(currentLocation)
    }


    def getAllAdminUsers() {
        def recipients = []
        def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
        if (roleAdmin) {
          def criteria = User.createCriteria()
          recipients = criteria.list {
              eq("active", true)
              roles {
                  eq("id", roleAdmin.id)
              }
          }
        }
        return recipients
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    def authenticate(username, password) {
        //def authenticated = false
        //def ldapEnabled = grailsApplication.config.openboxes.ldap.enabled?:false;
        //if (ldapEnabled) {
        //    authenticated = authenticateUsingLdap(username, password)
        //}
        //return authenticated || authenticateUsingDatabase(username, password)
        return authenticateUsingDatabase(username, password)

    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    def authenticateUsingDatabase(username, password) {
        def userInstance = User.findByUsernameOrEmail(username, username)
        if (userInstance) {
            return (userInstance.password == password.encodeAsPassword() || userInstance.password == password)
        }
        return false
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    def authenticateUsingLdap(username, password) {
        def host = grailsApplication.config.openboxes.ldap.context.server.host?:"ldap.forumsys.com"
        def port = grailsApplication.config.openboxes.ldap.context.server.port?:389
        def bindUserDn = grailsApplication.config.openboxes.ldap.context.managerDn?:"cn=read-only-admin,dc=example,dc=com"
        def bindUserPassword = grailsApplication.config.openboxes.ldap.context.managerPassword?:"password"
        long responseTimeoutMillis = grailsApplication.config.openboxes.ldap.responseTimeoutMillis?:1000;
        def searchSubtree = grailsApplication.config.openboxes.ldap.search.searchSubtree
        String [] attributesToReturn = ["displayName", "password", "mail", "cn"] //grailsApplication.config.openboxes.ldap.search.attributesToReturn

        LDAPConnection ldapConnection

        def result = [:]
        result.success = false

        try {

            ldapConnection = new LDAPConnection(host, port, bindUserDn, bindUserPassword);
            //ldapConnection = new LDAPConnection("ldap.forumsys.com", 389, "cn=read-only-admin,dc=example,dc=com", "password");

            //Search user DN:
            def searchBaseDn = grailsApplication.config.openboxes.ldap.search.base;
            //def searchBaseDn = "dc=example,dc=com"

            // Search filter
            def filter

            // Check if the search filter has been overridden in Config.groovy or openboxes-config.properties
            def searchFilter = grailsApplication.config.openboxes.ldap.search.filter; // "(uid={0})"
            if (searchFilter) {
                filter = StringUtil.format(searchFilter, username);
                log.info ("Using custom filter ${searchFilter} => ${filter}")

            }
            else {
                filter = Filter.createEqualityFilter("uid",username);
                log.info ("Using default equality filter on uid ${filter}")
            }


            // Perform search request
            SearchRequest searchRequest = new SearchRequest(searchBaseDn, SearchScope.SUB, DereferencePolicy.NEVER, 0, 0, false, filter, attributesToReturn);
            SearchResult searchResult = ldapConnection.search(searchRequest);
            log.info("Search result: ${searchResult}")

            result.searchResult = searchResult

            // If we find any entries we'll try to bind using the found DN and the given password
            log.info(searchResult.getEntryCount() + " entries returned.");
            if (searchResult.entryCount >= 1) {
                String dn = searchResult.searchEntries[0].getDN()
                SimpleBindRequest bindRequest = new SimpleBindRequest(dn, password);
                bindRequest.setResponseTimeoutMillis(responseTimeoutMillis);
                BindResult bindResult = ldapConnection.bind(bindRequest);

                result.dn = dn
                result.bindResult = bindResult
                result.resultCode = bindResult.resultCode

                if(bindResult.resultCode == ResultCode.SUCCESS) {
                    log.info("LDAP authentication successful dn=${dn}");
                    //return result
                    return true
                }
                else {
                    log.info("LDAP authentication unsuccessful dn=${dn}, resultCode=${bindResult?.resultCode?.name}");
                }
            }

        } catch(LDAPException e) {
            log.info("LDAP exception on bind: "  + e.message);

        } finally {
            ldapConnection.close()
        }
        //return result
        return false
    }



//        Server: ldap.forumsys.com
//        Port: 389
//        Bind DN: cn=read-only-admin,dc=example,dc=com
//        Bind Password: password
//        All user passwords are password.

    static boolean authenticateUsingLdap2(String username, String password) throws LDAPException {
        //LDAPConnection ldap = new LDAPConnection("ldap.forumsys.com", 389);
        //SearchResult sr = ldap.search("dc=People,dc=example,dc=com", SearchScope.SUB, "(uid=" + username + ")");
        //LDAPConnection ldap = new LDAPConnection("ldap.forumsys.com", 389);
        //SearchResult sr = ldap.search("ou=scientists,dc=example,dc=com", SearchScope.SUB, "(uid=" + username + ")");
        //println "search results " + sr
        //if (sr.getEntryCount() == 0)
        //    return false;

        //String dn = sr.getSearchEntries().get(0).getDN();

        try {
            //ldap = new LDAPConnection("ldap.example.com", 389, dn, password);
            LDAPConnection ldap = new LDAPConnection("ldap.forumsys.com", 389, username, password);
            return true;
        }
        catch (LDAPException e) {
            if (e.getResultCode() == ResultCode.INVALID_CREDENTIALS)
                return false;

            throw e;
        }
        return true
    }





    boolean authenticateUsingLdap3(dn, password) throws LDAPException {

        SocketFactory socketFactory;
        LDAPConnection ldapConnection = null;

        try {
            // Use no key manager, and trust all certificates. This would not be used
            // in non-trivial code.
            //SSLUtil sslUtil = new SSLUtil(null,new TrustAllTrustManager());

            // Create the socket factory that will be used to make a secure
            // connection to the server.
            //socketFactory = sslUtil.createSSLSocketFactory();
            //ldapConnection = new LDAPConnection(socketFactory,HOSTNAME,PORT);

            ldapConnection = new LDAPConnection("ldap.forumsys.com", 389);

        } catch(LDAPException e) {
            log.error("LDAP exception", e);
            //System.exit(e.getResultCode().intValue());

        } catch(GeneralSecurityException e) {
            log.error("General security exception: " + e.message, e);
            //System.exit(1);
        }

        try {
            //String dn = "uid=user.1,ou=people,dc=example,dc=com";
            //String password = "password";
            long maxResponseTimeMillis = 1000;

            BindRequest bindRequest = new SimpleBindRequest(dn,password);
            bindRequest.setResponseTimeoutMillis(maxResponseTimeMillis);
            BindResult bindResult = ldapConnection.bind(bindRequest);

            if(bindResult.resultCode == ResultCode.SUCCESS) {
                log.info("authentication successful");
                return true
            }

            log.info("bindResult: " + bindResult);
            log.info("diagnosticMessage: " + bindResult.diagnosticMessage);
            log.info("properties: " + bindResult.properties);
            log.info("resultCode: " + bindResult.resultCode);


        } catch(LDAPException e) {
            log.info("LDAP exception on bind: "  + e.message);
            return false
        } finally {
            ldapConnection.close();
        }
        return false
    }






    def boolean isUserInGroup(String groupDn, String userDn) throws LDAPException {
        boolean ret = false;

        LDAPConnection ldapConnection
        try {
            ldapConnection = new LDAPConnection("ldap.forumsys.com", 389, "cn=read-only-admin,dc=example,dc=com", "password");
            Entry groupEntry = ldapConnection.getEntry(groupDn);
            log.info("groupDn: " + groupDn)

            String[] memberValues = groupEntry.getAttributeValues("uniqueMember");
            log.info("memberValues: " + memberValues)
            if (memberValues != null) {
                DN ldapUserDn = new DN(userDn);
                for (String memberEntryDnString : memberValues) {
                    log.info("memberEntryDnString: " + memberEntryDnString)
                    DN memberEntryDn = new DN(memberEntryDnString);
                    if (memberEntryDn.equals(ldapUserDn)) {
                        ret = true;
                        break;
                    }
                }
            }
        } catch (LDAPException e) {
            log.error("LDAP exception occurred " + e.message, e)
        } finally {
            ldapConnection.close();
        }
        return ret;
    }




    def getRoles(String rolesDN, String userDN) throws LDAPException {

        Set<String> aclList = new LinkedHashSet<String>();
        LDAPConnection ldapConnection
        try {
            ldapConnection = new LDAPConnection("ldap.forumsys.com", 389, "cn=read-only-admin,dc=example,dc=com", "password");
            //LDAPConnection connection = connectionFactory.getLDAPConnection();
            SearchRequest searchRequest = new SearchRequest(rolesDN, SearchScope.SUB, Filter.createEqualityFilter("uniqueMember", userDN));

            SearchResult sr = ldapConnection.search(searchRequest);
            log.info("Search result: ${sr}")
            for (SearchResultEntry sre : sr.getSearchEntries()) {
                log.info("Search result entry: ${sre}")
                aclList.add(sre.getDN());
            }
        } catch (LDAPException e) {
            log.error("LDAP exception occurred " + e.message, e)

        } finally {
            ldapConnection.close();
        }

        return aclList;
    }


    def getMembersOfGroup(groupDn) {
        List<Entry> entryList = new ArrayList();
        LDAPConnection ldapConnection
        try {
            ldapConnection = new LDAPConnection("ldap.forumsys.com", 389, "cn=read-only-admin,dc=example,dc=com", "password");
            long responseTimeoutMillis = grailsApplication.config.openboxes.ldap.responseTimeoutMillis?:1000;
            SearchRequest searchRequest = new SearchRequest(groupDn, SearchScope.BASE, "(&)", "isMemberOf");
            searchRequest.setResponseTimeoutMillis(responseTimeoutMillis);

            def userEntry = ldapConnection.getEntry(groupDn)
            log.info("userEntry: ${userEntry}")
            String[] memberValues = userEntry.getAttributeValues("uniqueMember");
            log.info("memberValues: ${memberValues}")
            if (memberValues != null) {
                DNEntrySource entrySource = new DNEntrySource(ldapConnection, memberValues);
                log.info("entrySource: ${entrySource}")
                while (true) {
                    Entry memberEntry = entrySource.nextEntry();
                    if (memberEntry == null) {
                        break;
                    }
                    entryList.add(memberEntry.DN);
                }
            }
            /*
            SearchResult searchResult = ldapConnection.search(searchRequest);
            log.info("searchResult: ${searchResult}")
            if(searchResult.getEntryCount() == 1) {
                Entry entry = searchResult.getSearchEntry(userDN);
                log.info("entry: ${entry}")
                //return getAttributeValues("isMemberOf");
            }
            else {
                log.info("No search results found")

            }
            */
        } catch (LDAPException e) {
            log.error("LDAP exception occurred " + e.message, e)
        } finally {
            ldapConnection.close()
        }

        return entryList
    }

    def getUserGroups(userDn) {
        List<Entry> entryList = new ArrayList();
        LDAPConnection ldapConnection
        try {
            ldapConnection = new LDAPConnection("ldap.forumsys.com", 389, "cn=read-only-admin,dc=example,dc=com", "password");
            long responseTimeoutMillis = grailsApplication.config.openboxes.ldap.responseTimeoutMillis?:1000;
            SearchRequest searchRequest = new SearchRequest(userDn, SearchScope.BASE, "(&)", "isMemberOf");
            searchRequest.setResponseTimeoutMillis(responseTimeoutMillis);

            /*
            def userEntry = ldapConnection.getEntry(userDn)
            log.info("userEntry: ${userEntry}")
            String[] memberValues = userEntry.getAttributeValues("isMemberOf");
            log.info("memberValues: ${memberValues}")
            if (memberValues != null) {
                DNEntrySource entrySource = new DNEntrySource(ldapConnection, memberValues);
                log.info("entrySource: ${entrySource}")
                while (true) {
                    Entry memberEntry = entrySource.nextEntry();
                    if (memberEntry == null) {
                        break;
                    }
                    entryList.add(memberEntry.DN);
                }
            }
            */

            SearchResult searchResult = ldapConnection.search(searchRequest);
            log.info("searchResult: ${searchResult}")
            if(searchResult.getEntryCount() == 1) {
                Entry entry = searchResult.getSearchEntry(userDn);
                log.info("entry: ${entry}")
                //return getAttributeValues("isMemberOf");
            }
            else {
                log.info("No search results found")

            }

        } catch (LDAPException e) {
            log.error("LDAP exception occurred " + e.message, e)
        } finally {
            ldapConnection.close()
        }

        return entryList
    }




}
