/*
 * Copyright (c) 2011 Lockheed Martin Corporation
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
package org.eurekastreams.server.action.execution;

import java.io.Serializable;
import java.util.List;

import org.eurekastreams.commons.actions.ExecutionStrategy;
import org.eurekastreams.commons.actions.context.Principal;
import org.eurekastreams.commons.actions.context.PrincipalActionContext;
import org.eurekastreams.commons.exceptions.ExecutionException;
import org.eurekastreams.server.persistence.mappers.DomainMapper;
import org.eurekastreams.server.persistence.mappers.GetAllPersonIdsWhoHaveGroupCoordinatorAccess;
import org.eurekastreams.server.persistence.mappers.cache.PopulateOrgChildWithSkeletonParentOrgsCacheMapper;
import org.eurekastreams.server.persistence.mappers.stream.GetDomainGroupsByShortNames;
import org.eurekastreams.server.search.modelview.DomainGroupModelView;
import org.eurekastreams.server.search.modelview.PersonModelView;

/**
 * Return DomainGroupModelView for provided group shortName.
 * 
 */
public class GetDomainGroupModelViewByShortNameExectution implements ExecutionStrategy<PrincipalActionContext>
{
    /**
     * Mapper used to look up the group.
     */
    private GetDomainGroupsByShortNames groupByShortNameMapper;

    /**
     * Mapper to populate the parent org of people with skeleton orgs from cache.
     */
    private PopulateOrgChildWithSkeletonParentOrgsCacheMapper populateOrgChildWithSkeletonParentOrgsCacheMapper;

    /**
     * Mapper to get all person ids that have group coordinator access for a given group.
     */
    private GetAllPersonIdsWhoHaveGroupCoordinatorAccess groupCoordinatorIdsDAO;

    /**
     * Strategy to retrieve the banner id if it is not directly configured.
     */
    @SuppressWarnings("unchecked")
    private GetBannerIdByParentOrganizationStrategy getBannerIdStrategy;

    /**
     * Mapper to get followers for a group.
     */
    private DomainMapper<Long, List<Long>> groupFollowerIdsMapper;

    /**
     * Get ids for direct group coordinators.
     */
    private DomainMapper<Long, List<Long>> groupCoordinatorIdsByGroupIdMapper;

    /**
     * Get PersonModelViews by id.
     */
    private DomainMapper<List<Long>, List<PersonModelView>> personModelViewsByIdMapper;

    /**
     * Constructor.
     * 
     * @param inGroupByShortNameMapper
     *            injecting the mapper.
     * @param inPopulateOrgChildWithSkeletonParentOrgsCacheMapper
     *            mapper to populate parent orgs with skeleton
     * @param inGroupCoordinatorIdsDAO
     *            Mapper to get all person ids that have group coordinator access for a given group.
     * @param inGetBannerIdStrategy
     *            Instance of the {@link GetBannerIdByParentOrganizationStrategy}.
     * @param inGroupFollowerIdsMapper
     *            Instance of the {@link GetGroupFollowerIds}.
     * @param inGroupCoordinatorIdsByGroupIdMapper
     *            Get ids for direct group coordinators.
     * @param inPersonModelViewsByIdMapper
     *            Get PersonModelViews by id.
     */
    @SuppressWarnings("unchecked")
    public GetDomainGroupModelViewByShortNameExectution(
            final GetDomainGroupsByShortNames inGroupByShortNameMapper,
            final PopulateOrgChildWithSkeletonParentOrgsCacheMapper inPopulateOrgChildWithSkeletonParentOrgsCacheMapper,
            final GetAllPersonIdsWhoHaveGroupCoordinatorAccess inGroupCoordinatorIdsDAO,
            final GetBannerIdByParentOrganizationStrategy inGetBannerIdStrategy,
            final DomainMapper<Long, List<Long>> inGroupFollowerIdsMapper,
            final DomainMapper<Long, List<Long>> inGroupCoordinatorIdsByGroupIdMapper,
            final DomainMapper<List<Long>, List<PersonModelView>> inPersonModelViewsByIdMapper)
    {
        groupByShortNameMapper = inGroupByShortNameMapper;
        populateOrgChildWithSkeletonParentOrgsCacheMapper = inPopulateOrgChildWithSkeletonParentOrgsCacheMapper;
        groupCoordinatorIdsDAO = inGroupCoordinatorIdsDAO;
        getBannerIdStrategy = inGetBannerIdStrategy;
        groupFollowerIdsMapper = inGroupFollowerIdsMapper;
        groupCoordinatorIdsByGroupIdMapper = inGroupCoordinatorIdsByGroupIdMapper;
        personModelViewsByIdMapper = inPersonModelViewsByIdMapper;
    }

    @Override
    public Serializable execute(final PrincipalActionContext inActionContext) throws ExecutionException
    {
        String shortName = (String) inActionContext.getParams();
        DomainGroupModelView result = groupByShortNameMapper.fetchUniqueResult(shortName);

        // set banner for group.
        result.setBannerEntityId(result.getId());
        if (result.getBannerId() == null)
        {
            getBannerIdStrategy.getBannerId(result.getParentOrganizationId(), result);
        }

        // short circuit here if restricted for user.
        if (!isAccessPermitted(inActionContext.getPrincipal(), result))
        {
            // convert to new limited model view to prevent data leakage as model view grows.
            DomainGroupModelView restricted = new DomainGroupModelView();
            restricted.setRestricted(true);
            restricted.setEntityId(result.getId());
            restricted.setBannerId(result.getBannerId());
            restricted.setName(result.getName());
            restricted.setShortName(result.getShortName());
            return restricted;
        }
        else
        {
            result.setRestricted(false);
        }

        result.setCoordinators(personModelViewsByIdMapper.execute(groupCoordinatorIdsByGroupIdMapper.execute(result
                .getId())));

        return result;
    }

    /**
     * Check whether this group has restricted access and whether the current user is allowed access.
     * 
     * @param inPrincipal
     *            user principal.
     * @param inGroup
     *            the group the user wants to view
     * @return true if this person is allowed to see this group, false otherwise
     */
    private boolean isAccessPermitted(final Principal inPrincipal, final DomainGroupModelView inGroup)
    {
        // if group is public or user is coordinator recursively or follower, return true, otherwise false.
        return (inGroup.isPublic() || groupCoordinatorIdsDAO.execute(inGroup.getId()).contains(inPrincipal.getId()) //
        || isUserFollowingGroup(inPrincipal.getId(), inGroup.getId()));

    }

    /**
     * Checks to see if user is following a group.
     * 
     * @param userId
     *            the user id being checked.
     * @param groupId
     *            the group being checked.
     * @return true if user is a follower, false otherwise.
     */
    private boolean isUserFollowingGroup(final long userId, final long groupId)
    {
        List<Long> ids = groupFollowerIdsMapper.execute(groupId);
        if (ids.contains(userId))
        {
            return true;
        }
        return false;
    }

}
