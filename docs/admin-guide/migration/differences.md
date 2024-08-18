# Differences between Migration Strategies


Upgrading a single application by either updating dependencies on the existing server or creating a new server falls
under different types of migration strategies, which can be specified as follows:

<div class="grid cards" markdown>

-   :material-cloud-plus:{ .lg .middle } __Parallel Migration__

    ---

    Setting up a new server environment with the updated dependencies and application, then migrating the application to this new environment.

    * [x] Reduced risk of downtime
    * [x] Easier rollback to the old server if issues arise
    * [x] Opportunity to test the new environment thoroughly before switching over
    * [ ] Requires more resources to maintain two environments temporarily
    * [ ] Can be more time-consuming and complex to set up


-   :material-cloud-sync:{ .lg .middle } __In-Place Upgrade__

    --- 

    Upgrading the application and its dependencies directly on the existing server.

    * [x] Minimal changes to the infrastructure
    * [x] Usually faster than setting up a new server
    * [x] Retains current configurations and settings
    * [ ] Risk of downtime and service disruption
    * [ ] Potential for incompatibility issues
    * [ ] Harder to rollback if something goes wrong

</div>
