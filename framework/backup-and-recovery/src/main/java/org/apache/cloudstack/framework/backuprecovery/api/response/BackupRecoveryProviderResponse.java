// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.framework.backuprecovery.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.framework.backuprecovery.impl.BackupRecoveryProviderVO;

@EntityReference(value = BackupRecoveryProviderVO.class)
public class BackupRecoveryProviderResponse extends BaseResponse {

    @SerializedName(ApiConstants.BACKUP_PROVIDER_ID)
    @Param(description = "id of the Backup and Recovery provider")
    private String id;

    @SerializedName(ApiConstants.PROVIDER)
    @Param(description = "name of the provider")
    private String providerName;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "internal name for the Backup and Recovery provider")
    private String name;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "id of the zone")
    private String zoneId;

    @SerializedName(ApiConstants.HOST_ID)
    @Param(description = "id of the host")
    private String hostId;

    public void setId(String id) {
        this.id = id;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
