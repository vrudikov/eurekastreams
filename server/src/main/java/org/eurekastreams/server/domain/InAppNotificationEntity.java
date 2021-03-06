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
package org.eurekastreams.server.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eurekastreams.commons.model.DomainEntity;
import org.hibernate.annotations.Table;

/**
 * A notification for display within the application.
 */
@Entity(name = "InAppNotification")
@Table(appliesTo = "InAppNotification", indexes = { // \n

// index for use by GetUnreadInAppNotificationCountsByUserId and
// GetInAppNotificationsByUserId. The latter only uses
// recipientId within a WHERE clause, so that needs to be first.
@org.hibernate.annotations.Index(name = "InAppNotification_recipientId_highPriority_isRead", // \n
columnNames = { "recipientId", "highPriority", "isRead" })

})
public class InAppNotificationEntity extends DomainEntity implements Serializable
{
    /** Fingerprint. */
    private static final long serialVersionUID = 6548226026108187196L;

    /**
     * Person to receive the notification. (optional in DB to allow a template notification to be created and cloned for
     * mass notifying - see InsertInAppNotificationForAllUsers.)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "recipientId")
    private Person recipient;

    /** Type of notification being sent. */
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private NotificationType notificationType;

    /** The date the notification was added. */
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date notificationDate = new Date();

    /** The text of the notification. */
    private String message;

    /**
     * A URL the notification will link to. May be absolute or relative (just an anchor for URLs in app).
     */
    private String url;

    /**
     * A count of how many notifications have been aggregated into this notification.
     */
    private int aggregationCount = 1;

    /** If high priority. */
    @Basic(optional = false)
    private boolean highPriority;

    /** If read. */
    @Basic(optional = false)
    private boolean isRead;

    /**
     * Type of entity from which the notification came (person, group, app, or NOTSET=system).
     */
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private EntityType sourceType = EntityType.NOTSET;

    /** Unique ID of source. */
    @Basic(optional = true)
    private String sourceUniqueId;

    /** Display name of source (to show in filters). */
    @Basic(optional = true)
    private String sourceName;

    /**
     * Type of entity whose avatar will be displayed with the notification (person, group, app, or NOTSET=system).
     */
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private EntityType avatarOwnerType = EntityType.NOTSET;

    /** Unique ID of entity whose avatar will be displayed. */
    @Basic(optional = true)
    private String avatarOwnerUniqueId;

    /**
     * Constructor.
     */
    public InAppNotificationEntity()
    {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     *            Object to copy.
     */
    public InAppNotificationEntity(final InAppNotificationEntity other)
    {
        recipient = other.recipient;
        notificationType = other.notificationType;
        notificationDate = other.notificationDate;
        message = other.message;
        url = other.url;
        highPriority = other.highPriority;
        isRead = other.isRead;
        sourceType = other.sourceType;
        sourceUniqueId = other.sourceUniqueId;
        sourceName = other.sourceName;
        avatarOwnerType = other.avatarOwnerType;
        avatarOwnerUniqueId = other.avatarOwnerUniqueId;
    }

    /**
     * @return the notificationType
     */
    public NotificationType getNotificationType()
    {
        return notificationType;
    }

    /**
     * @param inNotificationType
     *            the notificationType to set
     */
    public void setNotificationType(final NotificationType inNotificationType)
    {
        notificationType = inNotificationType;
    }

    /**
     * @return the notificationDate
     */
    public Date getNotificationDate()
    {
        return notificationDate;
    }

    /**
     * @param inNotificationDate
     *            the notificationDate to set
     */
    public void setNotificationDate(final Date inNotificationDate)
    {
        notificationDate = inNotificationDate;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param inMessage
     *            the message to set
     */
    public void setMessage(final String inMessage)
    {
        message = inMessage;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param inUrl
     *            the url to set
     */
    public void setUrl(final String inUrl)
    {
        url = inUrl;
    }

    /**
     * @return the priority
     */
    public boolean isHighPriority()
    {
        return highPriority;
    }

    /**
     * @param inPriority
     *            the priority to set
     */
    public void setHighPriority(final boolean inPriority)
    {
        highPriority = inPriority;
    }

    /**
     * @return the isRead
     */
    public boolean isRead()
    {
        return isRead;
    }

    /**
     * @param inIsRead
     *            the isRead to set
     */
    public void setRead(final boolean inIsRead)
    {
        isRead = inIsRead;
    }

    /**
     * @return the sourceType
     */
    public EntityType getSourceType()
    {
        return sourceType;
    }

    /**
     * @param inSourceType
     *            the sourceType to set
     */
    public void setSourceType(final EntityType inSourceType)
    {
        sourceType = inSourceType;
    }

    /**
     * @return the sourceUniqueId
     */
    public String getSourceUniqueId()
    {
        return sourceUniqueId;
    }

    /**
     * @param inSourceUniqueId
     *            the sourceUniqueId to set
     */
    public void setSourceUniqueId(final String inSourceUniqueId)
    {
        sourceUniqueId = inSourceUniqueId;
    }

    /**
     * @return the sourceName
     */
    public String getSourceName()
    {
        return sourceName;
    }

    /**
     * @param inSourceName
     *            the sourceName to set
     */
    public void setSourceName(final String inSourceName)
    {
        sourceName = inSourceName;
    }

    /**
     * @return the avatarOwnerType
     */
    public EntityType getAvatarOwnerType()
    {
        return avatarOwnerType;
    }

    /**
     * @param inAvatarOwnerType
     *            the avatarOwnerType to set
     */
    public void setAvatarOwnerType(final EntityType inAvatarOwnerType)
    {
        avatarOwnerType = inAvatarOwnerType;
    }

    /**
     * @return the avatarOwnerUniqueId
     */
    public String getAvatarOwnerUniqueId()
    {
        return avatarOwnerUniqueId;
    }

    /**
     * @param inAvatarOwnerUniqueId
     *            the avatarOwnerUniqueId to set
     */
    public void setAvatarOwnerUniqueId(final String inAvatarOwnerUniqueId)
    {
        avatarOwnerUniqueId = inAvatarOwnerUniqueId;
    }

    /**
     * @return the recipient
     */
    public Person getRecipient()
    {
        return recipient;
    }

    /**
     * @param inRecipient
     *            the recipient to set
     */
    public void setRecipient(final Person inRecipient)
    {
        recipient = inRecipient;
    }

    /**
     * @return the aggregationCount
     */
    public int getAggregationCount()
    {
        return aggregationCount;
    }

    /**
     * @param inAggregationCount
     *            the aggregationCount to set
     */
    public void setAggregationCount(final int inAggregationCount)
    {
        aggregationCount = inAggregationCount;
    }
}
