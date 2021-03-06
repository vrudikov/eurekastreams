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
package org.eurekastreams.web.client.ui.common.pagedlist;

import org.eurekastreams.server.domain.dto.GalleryTabTemplateDTO;
import org.eurekastreams.web.client.ui.common.GalleryTabTemplateDTOPanel;

import com.google.gwt.user.client.ui.Panel;

/**
 * The theme gallery renderer.
 * 
 */
public class GalleryTabTemplateDTORenderer implements ItemRenderer<GalleryTabTemplateDTO>
{
    /**
     * Render the panel.
     * 
     * @param item
     *            the theme.
     * @return the panel.
     */
    public Panel render(final GalleryTabTemplateDTO item)
    {
        return new GalleryTabTemplateDTOPanel(item);
    }

}