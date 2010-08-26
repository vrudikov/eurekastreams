/*
 * Copyright (c) 2010 Lockheed Martin Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurekastreams.server.service.actions.strategies.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.eurekastreams.commons.logging.LogFactory;
import org.eurekastreams.server.domain.Person;
import org.eurekastreams.server.persistence.mappers.ldap.LdapGroup;
import org.eurekastreams.server.persistence.mappers.ldap.LdapLookup;
import org.eurekastreams.server.persistence.mappers.requests.LdapLookupRequest;
import org.eurekastreams.server.service.actions.strategies.PersonLookupStrategy;
import org.springframework.ldap.core.DistinguishedName;

/**
 * Person lookup strategy that, given an ldap group, returns all members of that group and subgroups.
 * 
 */
public class PersonLookupViaMembership implements PersonLookupStrategy
{
    /**
     * Logger.
     */
    private Log log = LogFactory.make();

    /**
     * Finds group(s) by name.
     */
    private LdapLookup<LdapGroup> groupMapper;

    /**
     * Finds group(s) by membership.
     */
    private LdapLookup<LdapGroup> subGroupMapper;

    /**
     * Finds people by membership.
     */
    private LdapLookup<Person> directGroupMemberMapper;

    /**
     * Base DN for LDAP queries.
     */
    private String baseLdapPath;

    /**
     * Constructor.
     * 
     * @param inGroupMapper
     *            Finds group(s) by name.
     * @param inSubGroupMapper
     *            Finds group(s) by membership.
     * @param inDirectGroupMemberMapper
     *            Finds people by membership.
     * @param inBaseLdapPath
     *            Base DN for LDAP queries.
     */
    public PersonLookupViaMembership(final LdapLookup<LdapGroup> inGroupMapper,
            final LdapLookup<LdapGroup> inSubGroupMapper, final LdapLookup<Person> inDirectGroupMemberMapper,
            final String inBaseLdapPath)
    {
        groupMapper = inGroupMapper;
        subGroupMapper = inSubGroupMapper;
        directGroupMemberMapper = inDirectGroupMemberMapper;
        baseLdapPath = inBaseLdapPath;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public List<Person> findPeople(final String inSearchString, final int inResultsUpperBound)
    {
        // get all groups (recursively) to search for direct members.
        Collection<LdapGroup> allGroups = getGroups(inSearchString);

        // Person collecting "bucket" to eliminate duplicate results.
        HashMap<String, Person> personBucket = new HashMap<String, Person>();

        // search each group for direct members and add them to bucket.
        for (LdapGroup lg : allGroups)
        {
            List<Person> people = directGroupMemberMapper.execute(new LdapLookupRequest(getFullDnAsString(lg), lg
                    .getDistinguishedName().toCompactString()));

            // throw all results into map to eliminate duplicates.
            for (Person p : people)
            {
                personBucket.put(p.getAccountId(), p);
            }
        }

        if (log.isInfoEnabled())
        {
            log.info("Found " + personBucket.size() + " unique people from searching " + allGroups.size() + " groups.");
        }

        return new ArrayList<Person>(personBucket.values());
    }

    /**
     * Method to return {@link LdapGroup}s and subgroups for given searchString.
     * 
     * @param inSearchString
     *            search string for groups.
     * @return Map<String, {@link LdapGroup}> result of query, where String is {@link LdapGroup} distinguished name.
     */
    private Collection<LdapGroup> getGroups(final String inSearchString)
    {
        // map used to collect all groups keyed by their dn. this eliminates duplicates in results.
        HashMap<String, LdapGroup> allGroups = new HashMap<String, LdapGroup>();

        // get the groups by name.
        List<LdapGroup> topLevelGroups = groupMapper.execute(new LdapLookupRequest(inSearchString));

        if (log.isDebugEnabled())
        {
            for (LdapGroup lg : topLevelGroups)
            {
                log.debug("Found " + lg.getDistinguishedName().toCompactString() + " as top level group.");
            }
        }

        // for each group found add to list of groups then find all subgroups and add them.
        for (LdapGroup lg : topLevelGroups)
        {
            allGroups.put(lg.getDistinguishedName().toCompactString(), lg);
            addSubGroups(lg, allGroups);
        }

        return allGroups.values();
    }

    /**
     * Recursive method that finds all subgroups for given {@link LdapGroup}.
     * 
     * @param inParentGroup
     *            {@link LdapGroup} to find subgroups for.
     * @param inAllGroups
     *            The map of {@link LdapGroup}s to add subgroups to.
     */
    private void addSubGroups(final LdapGroup inParentGroup, final HashMap<String, LdapGroup> inAllGroups)
    {

        // get template key from relative path (pre-base ldap path prepend).
        String templateKey = inParentGroup.getDistinguishedName().toCompactString();

        List<LdapGroup> subGroups = subGroupMapper.execute(new LdapLookupRequest(getFullDnAsString(inParentGroup),
                templateKey));

        // call once here to avoid multiple calls in loop.
        boolean logDebug = log.isDebugEnabled();

        for (LdapGroup lg : subGroups)
        {
            inAllGroups.put(lg.getDistinguishedName().toCompactString(), lg);

            if (logDebug)
            {
                log.debug("Found " + lg.getDistinguishedName().toCompactString() + " as direct subGroup of "
                        + inParentGroup.getDistinguishedName().toCompactString());
            }

            addSubGroups(lg, inAllGroups);
        }

        return;
    }

    /**
     * returns relative dn with base ldap path prepended.
     * 
     * @param inLdapGroup
     *            {@link LdapGroup} to get full dn for.
     * @return relative dn with base ldap path prepended.
     */
    private String getFullDnAsString(final LdapGroup inLdapGroup)
    {
        DistinguishedName dn = new DistinguishedName(inLdapGroup.getDistinguishedName());
        dn.prepend(new DistinguishedName(baseLdapPath));
        return dn.toCompactString();
    }

}
