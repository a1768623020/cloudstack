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

package org.apache.cloudstack.framework.backuprecovery.api;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.framework.backuprecovery.BackupRecoveryManager;
import org.apache.cloudstack.framework.backuprecovery.api.response.BackupRecoveryProviderResponse;
import org.apache.cloudstack.framework.backuprecovery.impl.BackupRecoveryProviderVO;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@APICommand(name = ListBackupRecoveryProvidersCmd.APINAME,
        description = "Lists Backup and Recovery providers",
        responseObject = BackupRecoveryProviderResponse.class, since = "4.12.0",
        authorized = {RoleType.Admin})
public class ListBackupRecoveryProvidersCmd extends BaseListCmd {

    public static final String APINAME = "listBackupRecoveryProviders";

    @Inject
    BackupRecoveryManager backupRecoveryManager;

    @Parameter(name=ApiConstants.ZONE_ID, type=BaseCmd.CommandType.UUID, entityType = ZoneResponse.class,
            description="the ID of the zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.PROVIDER, type = CommandType.STRING, entityType = BackupRecoveryProviderResponse.class,
            description = "the Backup and Recovery provider type")
    private String provider;

    public Long getZoneId() {
        return zoneId;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public void execute() throws ServerApiException, ConcurrentOperationException {
        try {
            List<BackupRecoveryProviderVO> providers = backupRecoveryManager.listBackupRecoveryProviders(this);
            ListResponse<BackupRecoveryProviderResponse> response = new ListResponse<BackupRecoveryProviderResponse>();
            List<BackupRecoveryProviderResponse> providersResponse = new ArrayList<BackupRecoveryProviderResponse>();

            if (CollectionUtils.isNotEmpty(providers)) {
                for (BackupRecoveryProviderVO providerVO : providers) {
                    BackupRecoveryProviderResponse providerResponse = backupRecoveryManager.createBackupRecoveryProviderResponse(providerVO);
                    providersResponse.add(providerResponse);
                }
            }

            response.setResponses(providersResponse, providersResponse.size());
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, e.getMessage());
        } catch (CloudRuntimeException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return APINAME.toLowerCase() + BaseCmd.RESPONSE_SUFFIX;
    }
}
