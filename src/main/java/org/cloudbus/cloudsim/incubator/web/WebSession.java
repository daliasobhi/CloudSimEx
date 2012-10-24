package org.cloudbus.cloudsim.incubator.web;

import org.cloudbus.cloudsim.incubator.util.Id;

/**
 * A web session is a session established between a user, and application server
 * (deployed in a VM) and a database server (deployed in another VM). Throughout
 * it's lifetime a session continuously generates workload on the servers
 * deployed in these virtual machines. The workload is represented as cloudlets,
 * which are sent to the assigned servers.
 * 
 * <br/>
 * 
 * To achieve this a web session generates two consequent "steams" of cloudlets
 * and directs to the servers. These streams are based on two instances of
 * {@link IGenerator} passed when constructing the session. The session takes
 * care to synchronize the two generators, so that consequent cloudlets from the
 * two generators are executed at the same pace. Thus if one of the servers is
 * performing better than the other, this will not be reflected in a quicker
 * exhaustion of its respective generator.
 * 
 * <br/>
 * 
 * Since the session utilizes {@link IGenerator} instances, which are unaware of
 * the simulation time, it needs to notify them of how time changes. By design
 * (to improve testability) web sessions are also unaware of simulation time.
 * Thus they need to be notified by an external "clock entity" of the simulation
 * time at a predefined period of time. This is done by the
 * {@link WebSession.notifyOfTime} method.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
public class WebSession {

    private IGenerator<? extends WebCloudlet> appServerCloudLets;
    private IGenerator<? extends WebCloudlet> dbServerCloudLets;

    private WebCloudlet currentAppServerCloudLet = null;
    private WebCloudlet currentDBServerCloudLet = null;

    private Integer appVmId = null;
    private Integer dbVmId = null;

    private final int sessionId;

    /**
     * Creates a new instance with the specified cloudlet generators.
     * 
     * @param appServerCloudLets
     *            - a generator for cloudlets for the application server. Must
     *            not be null.
     * @param dbServerCloudLets
     *            - a generator for cloudlets for the db server. Must not be
     *            null.
     */
    public WebSession(final IGenerator<? extends WebCloudlet> appServerCloudLets,
	    final IGenerator<? extends WebCloudlet> dbServerCloudLets) {
	super();
	this.appServerCloudLets = appServerCloudLets;
	this.dbServerCloudLets = dbServerCloudLets;
	sessionId = Id.pollId(getClass());
    }

    /**
     * Creates two cloudlets to submit to the virtual machines. The first
     * cloudlet is for the application server, the second - for the database. If
     * at this time no cloudlets should be sent to the servers null is returned.
     * 
     * @param currTime
     *            - the current time of the simulation.
     * @return the result as described above.
     */
    public WebCloudlet[] pollCloudlets(final double currTime) {

	WebCloudlet[] result = null;
	boolean appCloudletFinished = currentAppServerCloudLet == null
		|| currentAppServerCloudLet.isFinished();
	boolean dbCloudletFinished = currentDBServerCloudLet == null
		|| currentDBServerCloudLet.isFinished();
	boolean appServerNextReady = !appServerCloudLets.isEmpty()
		&& appServerCloudLets.peek().getIdealStartTime() <= currTime;
	boolean dbServerNextReady = !dbServerCloudLets.isEmpty()
		&& dbServerCloudLets.peek().getIdealStartTime() <= currTime;

	if (appCloudletFinished && dbCloudletFinished && appServerNextReady && dbServerNextReady) {
	    result = new WebCloudlet[] {
		    appServerCloudLets.poll(),
		    dbServerCloudLets.poll() };
	    currentAppServerCloudLet = result[0];
	    currentDBServerCloudLet = result[1];

	    currentAppServerCloudLet.setVmId(appVmId);
	    currentDBServerCloudLet.setVmId(dbVmId);

	    currentAppServerCloudLet.setSessionId(getSessionId());
	    currentDBServerCloudLet.setSessionId(getSessionId());
	}
	return result;
    }

    /**
     * NOTE!!! - used only for test purposes.
     * 
     * @return - the current app server cloudlet
     */
    /* package access */WebCloudlet getCurrentAppServerCloudLet() {
	return currentAppServerCloudLet;
    }

    /**
     * NOTE!!! - used only for test purposes.
     * 
     * @return - the current db server cloudlet.
     */
    /* package access */WebCloudlet getCurrentDBServerCloudLet() {
	return currentDBServerCloudLet;
    }

    /**
     * Gets notified of the current time.
     * 
     * @param time
     *            - the current CloudSim time.
     */
    public void notifyOfTime(final double time) {
	appServerCloudLets.notifyOfTime(time);
	dbServerCloudLets.notifyOfTime(time);
    }

    /**
     * Returns the id of the VM hosting the application server.
     * 
     * @return the id of the VM hosting the application server.
     */
    public int getAppVmId() {
	return appVmId;
    }

    /**
     * Sets the id of the VM hosting the app server. NOTE! you must set this id
     * before polling cloudlets.
     * 
     * @param appVmId
     *            - the id of the VM hosting the app server.
     */
    public void setAppVmId(int appVmId) {
	this.appVmId = appVmId;
    }

    /**
     * Returns the id of the VM hosting the db server.
     * 
     * @return the id of the VM hosting the db server.
     */
    public int getDbVmId() {
	return dbVmId;
    }

    /**
     * Sets the id of the VM hosting the db server. NOTE! you must set this id
     * before polling cloudlets.
     * 
     * @param dbVmId
     *            - the id of the VM hosting the db server.
     */
    public void setDbVmId(int dbVmId) {
	this.dbVmId = dbVmId;
    }

    /**
     * Returns the id of the session.
     * 
     * @return the id of the session.
     */
    public int getSessionId() {
	return sessionId;
    }

}
