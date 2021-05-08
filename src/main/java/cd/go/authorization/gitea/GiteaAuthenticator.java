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

package cd.go.authorization.gitea;

import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.TokenInfo;

import java.io.IOException;

public class GiteaAuthenticator {

    public GiteaUser authenticate(TokenInfo tokenInfo, AuthConfig authConfig) throws IOException {
        return authConfig.giteaConfiguration().giteaClient().user(tokenInfo);
    }

}
