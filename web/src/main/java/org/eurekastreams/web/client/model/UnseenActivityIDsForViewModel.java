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
package org.eurekastreams.web.client.model;

import java.util.ArrayList;

import org.eurekastreams.web.client.events.data.GotUnseenActivitiesCountResponseEvent;
import org.eurekastreams.web.client.ui.Session;

/**
 * Get the unseen activity for a view.
 * 
 */
public class UnseenActivityIDsForViewModel extends BaseModel implements Fetchable<String>
{
    /**
     * Singleton.
     */
    private static UnseenActivityIDsForViewModel model = new UnseenActivityIDsForViewModel();

    /**
     * Gets the singleton.
     * 
     * @return the singleton.
     */
    public static UnseenActivityIDsForViewModel getInstance()
    {
        return model;
    }

    /**
     * {@inheritDoc}
     */
    public void fetch(final String request, final boolean useClientCacheIfAvailable)
    {
        super.callReadAction("getActivityIDs", request, new OnSuccessCommand<ArrayList<Long>>()
        {
            public void onSuccess(final ArrayList<Long> response)
            {
                Session.getInstance().getEventBus()
                        .notifyObservers(new GotUnseenActivitiesCountResponseEvent(response));
            }
        }, useClientCacheIfAvailable);
    }
}
