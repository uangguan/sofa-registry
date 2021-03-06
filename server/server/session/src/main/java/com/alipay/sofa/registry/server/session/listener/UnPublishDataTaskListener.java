/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.listener;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.scheduler.task.UnPublishDataTask;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

/**
 *
 * @author kezhu.wukz
 * @version $Id: UnPublishDataTaskListener.java, v 0.1 2019-06-17 12:02 kezhu.wukz Exp $
 */
public class UnPublishDataTaskListener implements TaskListener {

    /**
     * store subscribers
     */
    @Autowired
    private Interests                           sessionInterests;

    /**
     * store publishers
     */
    @Autowired
    private DataStore                           sessionDataStore;

    @Autowired
    private Watchers                            sessionWatchers;

    /**
     * transfer data to DataNode
     */
    @Autowired
    private DataNodeService                     dataNodeService;

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    @Autowired
    private TaskProcessor                       dataNodeSingleTaskProcessor;

    @PostConstruct
    public void init() {
        singleTaskDispatcher = TaskDispatchers.createSingleTaskDispatcher(
            TaskDispatchers.getDispatcherName(TaskType.UN_PUBLISH_DATA_TASK.getName()), 100000, 32,
            1000, 1000, dataNodeSingleTaskProcessor);
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.UN_PUBLISH_DATA_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {
        SessionTask unPublishDataTask = new UnPublishDataTask(dataNodeService);

        unPublishDataTask.setTaskEvent(event);

        singleTaskDispatcher.dispatch(unPublishDataTask.getTaskId(), unPublishDataTask,
            unPublishDataTask.getExpiryTime());
    }

}