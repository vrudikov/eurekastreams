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
package org.eurekastreams.web.client.ui.common.tabs;

import java.util.HashMap;
import java.util.Map;

import org.eurekastreams.web.client.events.UpdateHistoryEvent;
import org.eurekastreams.web.client.history.CreateUrlRequest;
import org.eurekastreams.web.client.ui.Session;
import org.eurekastreams.web.client.ui.pages.master.StaticResourceBundle;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple tab. Just a name and a content widget. Can't be simpler than that. Oh, it also supports dragging and
 * dropping. I guess its not THAT simple.
 * 
 */
public class SimpleTab extends FlowPanel
{
    /**
     * The inner div. This is necessary because focuspanel can only have 1 child. Silly GWT.
     */
    private final FlowPanel panel = new FlowPanel();

    /**
     * focusPanel is the outside div for the tab. It is a focus panel for clickablility.
     */
    private final FocusPanel focusPanel = new FocusPanel();

    /**
     * The label is the text of the tab.
     */
    private final Label label;

    /**
     * The contents are what are displayed when the tab is clicked.
     */
    private Widget contents;

    /**
     * Is this tab draggable.
     */
    private boolean draggable = true;

    /** The tab's identifier. */
    private final String identifier;

    /**
     * Parameters to remove from the URL when the tab is selected. (This is a very primitive form of context handling.
     * The idea is that parameters set by controls on one tab aren't left in the URL to affect controls on another tab.
     */
    private String[] paramsToClear = {};

    /**
     * Parameters to set when this tab is selected.
     */
    private Map<String, String> paramsToSet = null;

    /**
     * Constructor with no contents. (Sometimes tabs do other things when clicked.)
     * 
     * @param inIdentifier
     *            the identifier of the tab (also used as the title).
     */
    public SimpleTab(final String inIdentifier)
    {
        this(inIdentifier, inIdentifier, null);
    }

    /**
     * Constructor.
     * 
     * @param inIdentifier
     *            the identifier of the tab (also used as the title).
     * @param inContents
     *            the contents to show when the tab is clicked.
     */
    public SimpleTab(final String inIdentifier, final Widget inContents)
    {
        this(inIdentifier, inIdentifier, inContents);
    }

    /**
     * Constructor.
     * 
     * @param inIdentifier
     *            the identifier of the tab.
     * @param inTitle
     *            The title to display on the tab.
     * @param inContents
     *            the contents to show when the tab is clicked.
     */
    public SimpleTab(final String inIdentifier, final String inTitle, final Widget inContents)
    {
        this.addStyleName(StaticResourceBundle.INSTANCE.coreCss().tab());
        focusPanel.add(panel);
        this.add(focusPanel);

        identifier = inIdentifier;

        label = new Label(inTitle);
        contents = inContents;

        panel.add(label);
    }

    /**
     * @param inParams
     *            List of URL parameters to clear when tab is selected.
     */
    public void setParamsToClear(final String... inParams)
    {
        paramsToClear = inParams;
    }

    /**
     * Set the params that will be added when this tab is selected.
     * 
     * @param inParamsToSet
     *            the paramsToSet to set
     */
    public void setParamsToSet(final Map<String, String> inParamsToSet)
    {
        paramsToSet = inParamsToSet;
    }

    /**
     * Set whether the tab is draggable.
     * 
     * @param inDraggable
     *            draggable.
     */
    public void setDraggable(final boolean inDraggable)
    {
        draggable = inDraggable;
    }

    /**
     * Init the tab.
     * 
     * @param key
     *            the history token key.
     */
    public void init(final String key)
    {
        focusPanel.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event)
            {
                if (getIdentifier() != null)
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(key, getIdentifier());
                    int length = paramsToClear.length;
                    for (int i = 0; i < length; i++)
                    {
                        params.put(paramsToClear[i], null);
                    }

                    if (paramsToSet != null)
                    {
                        length = paramsToSet.keySet().size();
                        for (String key : paramsToSet.keySet())
                        {
                            params.put(key, paramsToSet.get(key));
                        }
                    }
                    Session.getInstance().getEventBus().notifyObservers(
                            new UpdateHistoryEvent(new CreateUrlRequest(params, false)));
                }
            }
        });
    }

    /**
     * Make this tab draggable.
     * 
     * @param dragController
     *            the drag controller.
     */
    public void makeTabDraggable(final PickupDragController dragController)
    {
        if (draggable)
        {
            dragController.makeDraggable(this, label);
        }
    }

    /**
     * Set the contents manually.
     * 
     * @param inContents
     *            the contents.
     */
    public void setContents(final Widget inContents)
    {
        contents = inContents;
    }

    /**
     * Get the contents.
     * 
     * @return the contents.
     */
    public Widget getContents()
    {
        return contents;
    }

    /**
     * Gets the label for subclasses.
     * 
     * @return the label.
     */
    public Label getLabel()
    {
        return label;
    }

    /**
     * Gets the panel for subclasses.
     * 
     * @return the panel.
     */
    protected FlowPanel getPanel()
    {
        return panel;
    }

    /**
     * Gets the focuspanel for subclasses.
     * 
     * @return the focuspanel.
     */
    protected FocusPanel getFocusPanel()
    {
        return focusPanel;
    }

    /**
     * The identifier of the tab is how it is keyed in the system. Also, what will be displayed in the URL when clicked.
     * *IT MUST BE UNIQUE ACROSS ALL TABS*
     * 
     * @return the identifier.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Gets called when the tab is selected.
     */
    public void select()
    {
        addStyleName(StaticResourceBundle.INSTANCE.coreCss().active());
    }

    /**
     * Gets called when the tab is unselected.
     */
    public void unSelect()
    {
        removeStyleName(StaticResourceBundle.INSTANCE.coreCss().active());
    }

    /**
     * Renames the tab.
     * 
     * @param inTitle
     *            New name.
     */
    @Override
    public void setTitle(final String inTitle)
    {
        label.setText(inTitle);
    }
}
