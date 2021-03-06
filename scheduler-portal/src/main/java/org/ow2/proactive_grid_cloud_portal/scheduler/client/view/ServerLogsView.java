/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client.view;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.ServerLogsListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.ServerLogsController;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.ServerLogsModel;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;


/**
 * Displays the output of the selected job, or a task in the selected job
 * 
 * 
 * @author mschnoor
 *
 */
public class ServerLogsView extends AbstractOutputDisplayView<ServerLogsModel, ServerLogsController> implements ServerLogsListener{

    /** contains the layout */
    private Layout root = null;
    

    /**
     * Default constructor
     * @param controller
     */
    public ServerLogsView(ServerLogsController controller) {
        super(controller);
        this.refreshButtonLabel = "Fetch logs";
        this.refreshButtonTooltip = "Request fetching the Output for this job";
        this.noOutputMessage = "No logs available<br><br>"
                + "Click <strong>Fetch logs</strong> to retrieve logs for tasks<br>";
        
        controller.getModel().addServerlogsListener(this);
    }

    /**
     * @return the Widget to display, ready to be added in a container
     */
    public Layout build() {
        this.root = new VLayout();
        this.root.setWidth100();
        this.root.setHeight100();

        this.buildRefreshButton();

        this.buildTargetSelect();

        DynamicForm form = new DynamicForm();
        form.setColWidths("*", "*");
        form.setNumCols(2);
        form.setFields(this.targetSelect);

        HLayout buttons = new HLayout();
        buttons.setWidth100();
        buttons.setHeight(22);
        buttons.setMembersMargin(5);

        Canvas fill = new Canvas();
        fill.setWidth100();

        buttons.setMembers(form, fill, refreshButton);
        
        VLayout textlayout = this.buildOutputPane();

        this.root.addMember(buttons);
        this.root.addMember(textlayout);
        
        this.goToNoTargetState();

        return this.root;
    }

   
    
    @Override
    public void selectedJobUpdated(Job job) {   
        this.controller.refreshOutput();
    }
    

    @Override
    public void logsUpdated(String logs, String jobId) {
        if(jobId == null){
            this.goToNoTargetState();
        }
        else{
            this.goToTargetSelectedState();
            if(logs == null){
                this.goToUnavailableOutputState();
            }
            else{
                this.showContent(logs);
            }
        }
        
    }
    
}
