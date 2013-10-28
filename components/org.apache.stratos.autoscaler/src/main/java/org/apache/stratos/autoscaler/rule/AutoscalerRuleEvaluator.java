/*
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.autoscaler.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.AutoscalerContext;
import org.apache.stratos.autoscaler.client.cloud.controller.CloudControllerClient;
import org.apache.stratos.autoscaler.message.receiver.TopologyManager;
import org.apache.stratos.autoscaler.policy.PolicyManager;
import org.apache.stratos.autoscaler.policy.model.Partition;
import org.apache.stratos.messaging.domain.topology.Service;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * This class is responsible for evaluating the current details of topology, statistics, and health
 * status against the rules set(written in Drools)
 */
public class AutoscalerRuleEvaluator {
	
	private static final Log log = LogFactory.getLog(AutoscalerRuleEvaluator.class);
	
	private static AutoscalerRuleEvaluator instance = null;
	private static final String DRL_FILE_NAME = "autoscaler.drl";
	
	private KnowledgeBase kbase;
	private StatefulKnowledgeSession ksession;
	 
	private AutoscalerRuleEvaluator() {

		try {
			// load up the knowledge base
			kbase = readKnowledgeBase();
			ksession = kbase.newStatefulKnowledgeSession();
			ksession.setGlobal("$context", AutoscalerContext.getInstance());
			ksession.setGlobal("log", log);
			ksession.setGlobal("$manager", PolicyManager.getInstance());
			ksession.setGlobal("$topology", TopologyManager.getTopology());
			ksession.setGlobal("$evaluator", this);
		} catch (Throwable t) {
			log.error("Rule-Evaluator initialization error", t);
		}
	}
    
    
    public void evaluate(Service service){
        try {
        	log.info("Evaluating rule for service " + service.getServiceName());
			ksession.insert(service);
			ksession.fireAllRules();
		} catch (Exception e) {
			log.error("Rule evaluate error", e);
		}
    }
    
	public boolean delegateSpawn(Partition partition, String clusterId) {
		CloudControllerClient cloudControllerClient = new CloudControllerClient();
		try {
			cloudControllerClient.spawnAnInstance(partition, clusterId);
		} catch (Throwable e) {
			log.error("Cannot spawn an instance", e);
		}
		return false;
	}
	
	public boolean delegateTerminateAll(String clusterId) {
		CloudControllerClient cloudControllerClient = new CloudControllerClient();
		try {
			cloudControllerClient.terminateAll(clusterId);
			return true;
		} catch (Throwable e) {
			log.error("Cannot terminate instance", e);
		}
		return false;
	}

	public boolean delegateTerminate(Partition partition, String clusterId) {
		CloudControllerClient cloudControllerClient = new CloudControllerClient();
		try {
			cloudControllerClient.terminate(partition, clusterId);
			return true;
		} catch (Throwable e) {
			log.error("Cannot terminate instance", e);
		}
		return false;
	}

	public boolean delegateSpawn(Partition partition, String clusterId, int memberCountToBeIncreased) {
		CloudControllerClient cloudControllerClient = new CloudControllerClient();
		try {
			cloudControllerClient.spawnAnInstance(partition, clusterId);
			return true;
		} catch (Throwable e) {
			log.error("Cannot spawn an instance", e);
		}
		return false;
	}

    public static synchronized AutoscalerRuleEvaluator getInstance() {
            if (instance == null) {
                    instance = new AutoscalerRuleEvaluator ();
            }
            return instance;
    }
    
    private KnowledgeBase readKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        Resource resource = ResourceFactory.newClassPathResource(DRL_FILE_NAME);
		kbuilder.add(resource, ResourceType.DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                log.error(error.getMessage());
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

}