/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.gitea.executors;

import cd.go.authorization.gitea.annotation.MetadataHelper;
import cd.go.authorization.gitea.annotation.ProfileMetadata;
import cd.go.authorization.gitea.models.GiteaRoleConfiguration;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.List;

import static cd.go.authorization.gitea.utils.Util.GSON;


public class GetRoleConfigMetadataRequestExecutor implements RequestExecutor {

    public GoPluginApiResponse execute() throws Exception {
        final List<ProfileMetadata> authConfigMetadata = MetadataHelper.getMetadata(GiteaRoleConfiguration.class);
        return DefaultGoPluginApiResponse.success(GSON.toJson(authConfigMetadata));
    }
}
