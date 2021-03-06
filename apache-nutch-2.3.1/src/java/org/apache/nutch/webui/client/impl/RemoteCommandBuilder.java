/**
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
package org.apache.nutch.webui.client.impl;

import org.apache.nutch.webui.client.model.JobConfig;
import org.apache.nutch.webui.client.model.JobInfo.JobType;
import org.joda.time.Duration;

public class RemoteCommandBuilder {
  private JobConfig jobConfig = new JobConfig();
  private Duration timeout = Duration.standardSeconds(10);

  private RemoteCommandBuilder() {
  }

  public static RemoteCommandBuilder instance(JobType jobType) {
    return new RemoteCommandBuilder().withJobType(jobType);
  }

  public RemoteCommandBuilder withJobType(JobType jobType) {
    jobConfig.setType(jobType);
    return this;
  }

  public RemoteCommandBuilder withConfigId(String configId) {
    jobConfig.setConfId(configId);
    return this;
  }

  public RemoteCommandBuilder withCrawlId(String crawlId) {
    jobConfig.setCrawlId(crawlId);
    return this;
  }

  public RemoteCommandBuilder withArgument(String key, String value) {
    jobConfig.setArgument(key, value);
    return this;
  }

  public RemoteCommandBuilder withTimeout(Duration timeout) {
    this.timeout = timeout;
    return this;
  }

  public RemoteCommand build() {
    RemoteCommand remoteCommand = new RemoteCommand(jobConfig);
    remoteCommand.setTimeout(timeout);
    return remoteCommand;
  }
}
