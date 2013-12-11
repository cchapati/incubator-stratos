/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.autoscaler;

import java.util.*;

/**
 * Holds runtime data of a network partition.
 * @author nirmal
 *
 */
public class NetworkPartitionContext {

    private String id;

    private String defaultLbClusterId;

    private Map<String, String> serviceNameToLBClusterIdMap;

    private Map<String, String> clusterIdToLBClusterIdMap;

    private String partitionAlgorithm;


    //Following information will keep events details
    private float averageRequestsInFlight;
    private float requestsInFlightSecondDerivative;
    private float requestsInFlightGradient;

    //details required for partition selection algorithms
    private int currentPartitionIndex;
    private Map<String, Integer> partitionCountMap;

    //partitions of this network partition
    private Map<String, PartitionContext> partitionCtxts;

    public NetworkPartitionContext(String id) {

        super();
        this.id = id;
        this.setServiceToLBClusterId(new HashMap<String, String>());
        this.setClusterIdToLBClusterIdMap(new HashMap<String, String>());

    }

    public String getDefaultLbClusterId() {

        return this.defaultLbClusterId;

    }

    public void setDefaultLbClusterId(final String defaultLbClusterId) {

        this.defaultLbClusterId = defaultLbClusterId;

    }

    public String getLBClusterIdOfService(final String serviceName) {

        return (String) this.serviceNameToLBClusterIdMap.get(serviceName);

    }

    public Map<String, String> getServiceToLBClusterId() {

        return this.serviceNameToLBClusterIdMap;

    }

    public void setServiceToLBClusterId(final Map<String, String> serviceToLBClusterId) {

        this.serviceNameToLBClusterIdMap = serviceToLBClusterId;

    }

    public String getLBClusterIdOfCluster(final String clusterId) {

        return (String) this.clusterIdToLBClusterIdMap.get(clusterId);

    }

    public Map<String, String> getClusterIdToLBClusterIdMap() {

        return this.clusterIdToLBClusterIdMap;

    }

    public void setClusterIdToLBClusterIdMap(final Map<String, String> clusterIdToLBClusterIdMap) {

        this.clusterIdToLBClusterIdMap = clusterIdToLBClusterIdMap;

    }


    public boolean isLBExist(final String clusterId) {

        return clusterId != null &&
               (clusterId.equals(this.defaultLbClusterId) ||
                this.serviceNameToLBClusterIdMap.containsValue(clusterId) || this.clusterIdToLBClusterIdMap.containsValue(clusterId));

    }

    public boolean isDefaultLBExist() {

        return defaultLbClusterId != null;

    }

    public boolean isServiceLBExist(String serviceName) {

        return this.serviceNameToLBClusterIdMap.containsKey(serviceName) &&
                this.serviceNameToLBClusterIdMap.get(serviceName) != null;

    }

    public boolean isClusterLBExist(String clusterId) {

        return this.clusterIdToLBClusterIdMap.containsKey(clusterId) &&
                this.clusterIdToLBClusterIdMap.get(clusterId) != null;

    }

    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;

    }

    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NetworkPartitionContext)) {
            return false;
        }
        final NetworkPartitionContext other = (NetworkPartitionContext) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }



    public int getCurrentPartitionIndex() {
        return currentPartitionIndex;
    }

    public void setCurrentPartitionIndex(int currentPartitionIndex) {
        this.currentPartitionIndex = currentPartitionIndex;
    }

    public float getAverageRequestsInFlight() {
        return averageRequestsInFlight;
    }

    public void setAverageRequestsInFlight(float averageRequestsInFlight) {
        this.averageRequestsInFlight = averageRequestsInFlight;
    }

    public float getRequestsInFlightSecondDerivative() {
        return requestsInFlightSecondDerivative;
    }

    public void setRequestsInFlightSecondDerivative(float requestsInFlightSecondDerivative) {
        this.requestsInFlightSecondDerivative = requestsInFlightSecondDerivative;
    }

    public float getRequestsInFlightGradient() {
        return requestsInFlightGradient;
    }

    public void setRequestsInFlightGradient(float requestsInFlightGradient) {
        this.requestsInFlightGradient = requestsInFlightGradient;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void increaseMemberCountInPartitionBy(String partitionId, int count){

         partitionCountMap.put(partitionId, getMemberCount(partitionId) + count);
     }

     public void decreaseMemberCountInPartitionBy(String partitionId, int count){

         partitionCountMap.put(partitionId, getMemberCount(partitionId) - count);
     }

     public void addPartitionCount(String partitionId, int count){
         partitionCountMap.put(partitionId, count);
     }

     public void removePartitionCount(String partitionId){

         partitionCountMap.remove(partitionId);
     }

     public boolean partitionCountExists(String partitionId){
         return partitionCountMap.containsKey(partitionId);
     }

     public int getMemberCount(String partitionId){
         if(partitionCountMap.containsKey(partitionId)) {
             return partitionCountMap.get(partitionId);
         }
         return 0;
     }

    public Map<String, PartitionContext> getPartitionCtxts() {
        return partitionCtxts;
    }

    public PartitionContext getPartitionCtxt(String partitionId) {
        return partitionCtxts.get(partitionId);
    }

    public void setPartitionCtxts(Map<String, PartitionContext> partitionCtxts) {
        this.partitionCtxts = partitionCtxts;
    }

    public void addPartitionContext(PartitionContext partitionContext) {
        partitionCtxts.put(partitionContext.getPartitionId(), partitionContext);
    }

    public String getPartitionAlgorithm() {
        return partitionAlgorithm;
    }

    public void setPartitionAlgorithm(String partitionAlgorithm) {
        this.partitionAlgorithm = partitionAlgorithm;
    }
}