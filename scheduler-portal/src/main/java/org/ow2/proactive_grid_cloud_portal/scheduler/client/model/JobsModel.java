/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobSelectedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobsUpdatedListener;


/**
 * Model for the jobs logic.
 * @author the activeeon team.
 *
 */
public class JobsModel {

    /**
     * The jobs that are fetched from the server.
     */
    private Map<Integer, Job> jobs = null;

    /**
     * The jobs revision. Jobs are updated only if we fetch a greater revision number.
     */
    private long jobsRev = -1;

    /**
     * The current selected job.
     */
    private Job selectedJob = null;



    /**
     * Listener for the updates of the jobs list.
     */
    private ArrayList<JobsUpdatedListener> jobsUpdatedListeners = null;

    /**
     * Listener for the job selection changes.
     */
    private ArrayList<JobSelectedListener> jobSelectedListeners = null;

    /**
     * The parent model.
     */
    private ExecutionsModel parentModel;

    /**
     * The model for the job pagination logic.
     */
    private PaginationModel paginationModel;

    /**
     * Builds a jobs model from the scheduler parent model.
     * @param parentModel the scheduler parent model.
     */
    public JobsModel(ExecutionsModel parentModel) {
        this.parentModel = parentModel;
        this.jobsUpdatedListeners = new ArrayList<JobsUpdatedListener>();
        this.jobSelectedListeners = new ArrayList<JobSelectedListener>();
    }

    /**
     * @return local view of all the jobs known by the scheduler server
     */
    public Map<Integer, Job> getJobs() {
        return this.jobs;
    }

    /**
     * Empties the jobs list.
     */
    public void emptyJobs() {
        setJobs(null, -1, 0);
    }


    /**
     * Modifies the local joblist
     * triggers {@link JobsUpdatedListener#jobsUpdated(java.util.Map)}},
     * or {@link JobsUpdatedListener#jobsUpdating()} if <code>jobs</code> was null
     * 
     * @param jobs a jobset, or null
     * @param rev the revision of this jobset
     */
    public void setJobs(Map<Integer, Job> jobs, long rev, long totalJobs) {
        this.jobs = jobs;
        this.jobsRev = rev;
        boolean empty = false;

        if (jobs == null) {
            empty = true;
            this.jobs = new LinkedHashMap<Integer, Job>();
        }

        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobsUpdated(this.jobs, totalJobs);
            if (empty)
                listener.jobsUpdating();
        }

        if(this.selectedJob != null){
            Job oldSel = this.selectedJob;
            this.selectedJob = jobs.get(oldSel.getId());
            if(this.selectedJob == null){
                for(JobSelectedListener listener: this.jobSelectedListeners){
                    listener.jobUnselected();
                }
            }
            else{
                if (this.selectedJob != null && !this.selectedJob.isEqual(oldSel)) {
                    for(JobSelectedListener listener: this.jobSelectedListeners){
                        listener.selectedJobUpdated(this.selectedJob);
                    }
                }
            }
        }
    }

    /**
     * Notifies that a job has been submitted.
     * @param j
     */
    public void jobSubmitted(Job j) {
        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobSubmitted(j);
        }
    }


    /**
     * Notifies that jobs are updating.
     */
    public void jobsUpdating() {
        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobsUpdating();
        }
    }


    /**
     * Gets the jobs pagination model. 
     * @return the jobs pagination model.
     */
    public PaginationModel getPaginationModel() {
        return paginationModel;
    }

    /**
     * Sets the jobs pagination model.
     * @param jobsPaginationModel the jobs pagination model.
     */
    public void setPaginationModel(PaginationModel jobsPaginationModel) {
        this.paginationModel = jobsPaginationModel;
    }

    /**
     * Modifies the Job selection,
     * triggers a JobSelected event
     *
     */
    public void selectJob(Job job) {
        boolean selChanged = this.selectedJob == null || !this.selectedJob.equals(job);
        this.selectedJob = job;

        if(selChanged){
            // notify job selection listeners
            for (JobSelectedListener listener : this.jobSelectedListeners) {
                if (job == null)
                    listener.jobUnselected();
                else
                    listener.jobSelected(job);
            }

            this.parentModel.getParentModel().getTasksModel().selectTask(null);
        }
    }


    /**
     * @return the currently selected job
     */
    public Job getSelectedJob() {
        return this.selectedJob;
    }


    /**
     * @param jobId the Id of a Job
     * @return the corresponding job if known by the Model, or null
     */
    public Job getJob(int jobId) {
        for (Job j : this.jobs.values()) {
            if (j.getId() == jobId) {
                return j;
            }
        }
        return null;
    }


    /**
     * @return the revision id associated with the currently held JobBag
     */
    public long getJobsRevision() {
        return this.jobsRev;
    }


    /**
     * Add a listener for jobs list updates
     * @param listener a listener for jobs list updates.
     */
    public void addJobsUpdatedListener(JobsUpdatedListener listener) {
        this.jobsUpdatedListeners.add(listener);
    }

    /**
     * Add a listener for job selection changes.
     * @param listener a listener for job selection changes.
     */
    public void addJobSelectedListener(JobSelectedListener listener) {
        this.jobSelectedListeners.add(listener);
    }
}
