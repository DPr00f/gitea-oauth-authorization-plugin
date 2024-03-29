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

package cd.go.authorization.gitea.requests;

import cd.go.authorization.gitea.executors.AuthConfigValidateRequestExecutor;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

public class AuthConfigValidateRequest extends Request {
    private final GiteaConfiguration giteaConfiguration;

    public AuthConfigValidateRequest(GiteaConfiguration giteaConfiguration) {
        this.giteaConfiguration = giteaConfiguration;
    }

    public static final AuthConfigValidateRequest from(GoPluginApiRequest apiRequest) {
        return new AuthConfigValidateRequest(GiteaConfiguration.fromJSON(apiRequest.requestBody()));
    }

    public GiteaConfiguration giteaConfiguration() {
        return giteaConfiguration;
    }

    @Override
    public AuthConfigValidateRequestExecutor executor() {
        return new AuthConfigValidateRequestExecutor(this);
    }
}
