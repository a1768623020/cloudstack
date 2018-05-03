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
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.framework.backuprecovery.BackupRecoveryManager;
import org.apache.cloudstack.framework.backuprecovery.api.response.BackupRecoveryProviderPolicyResponse;
import org.apache.cloudstack.framework.backuprecovery.api.response.BackupRecoveryProviderResponse;

import javax.inject.Inject;
import java.util.List;

@APICommand(name = ListBackupRecoveryProviderPoliciesCmd.APINAME,
        description = "Lists Backup policies existing on the Backup and Recovery provider side",
        responseObject = BackupRecoveryProviderPolicyResponse.class, since = "4.12.0",
        authorized = {RoleType.Admin})
public class ListBackupRecoveryProviderPoliciesCmd extends BaseListCmd {

    public static final String APINAME = "listBackupRecoveryProviderPolicies";

    @Inject
    BackupRecoveryManager backupRecoveryManager;

    @Parameter(name = ApiConstants.BACKUP_PROVIDER_ID,
            type = BaseCmd.CommandType.UUID,
            entityType = BackupRecoveryProviderResponse.class,
            required = true,
            description = "Backup Recovery Provider ID")
    private Long providerId;

    public Long getProviderId() {
        return providerId;
    }

    @Override
    public void execute() throws ResourceUnavailableException, ServerApiException, ConcurrentOperationException {
        try {
            ListResponse<BackupRecoveryProviderPolicyResponse> response = new ListResponse<BackupRecoveryProviderPolicyResponse>();
            List<BackupRecoveryProviderPolicyResponse> policies = backupRecoveryManager.listBackupPolicies(this);
            if (policies == null) {
                throw new CloudRuntimeException("Error while retrieving backup provider policies");
            }
            response.setResponses(policies, policies.size());
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, e.getMessage());
        } catch (CloudRuntimeException | OperationTimedoutException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return APINAME.toLowerCase() + BaseCmd.RESPONSE_SUFFIX;
    }
}
