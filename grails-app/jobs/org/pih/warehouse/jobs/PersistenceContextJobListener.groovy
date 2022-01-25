/*
 * Copyright (c) 2011 the original author or authors.
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

package org.pih.warehouse.jobs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

public class PersistenceContextJobListener extends grails.plugin.quartz2.PersistenceContextJobListener {
	private static final transient Logger log = LoggerFactory.getLogger(PersistenceContextJobListener.class);
    PersistenceContextInterceptor persistenceInterceptor
	public static final transient String PERSITENCE_INIT = "gormSession";
	public static final String LISTENER_NAME = "persistenceContextJobListener"

	@Override
    public String getName() {
        return LISTENER_NAME
    }

	@Override
    public void jobToBeExecuted(JobExecutionContext context) {
		if( isInitPersistenceContext(context) ){
			persistenceInterceptor.init()
		}
    }

	@Override
    public void jobExecutionVetoed(JobExecutionContext context) {

	}

	@Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
        if( persistenceInterceptor && isInitPersistenceContext(context) ){
			try {
                persistenceInterceptor.flush()
                persistenceInterceptor.clear()
            } catch (Exception e) {
                log.error("Exception occurred while flushing persistence context for job ${context.jobDetail.key}: " + e.message, e)
				if (e.cause) {
                	log.fatal("Exception was caused by: " + e?.cause?.message, e?.cause)
				}
            } finally {
                try {
                    persistenceInterceptor.destroy()
                } catch (Exception e) {
                    log.fatal("Exception occurred while destroying persistence context for job ${context?.jobDetail?.key}: " + e.message, e)
                }
            }
		}
    }

	boolean isInitPersistenceContext(context){
		if(context.mergedJobDataMap.containsKey(PERSITENCE_INIT) && context.mergedJobDataMap.get(PERSITENCE_INIT) == false){
			return false
		}else{
			return true
		}
	}
}
