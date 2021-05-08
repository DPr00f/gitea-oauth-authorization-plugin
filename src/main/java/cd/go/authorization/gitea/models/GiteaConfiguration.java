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

package cd.go.authorization.gitea.models;

import cd.go.authorization.gitea.annotation.ProfileField;
import cd.go.authorization.gitea.annotation.Validatable;
import cd.go.authorization.gitea.client.GiteaClient;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import static cd.go.authorization.gitea.utils.Util.GSON;

public class GiteaConfiguration implements Validatable {
    @Expose
    @SerializedName("ApplicationId")
    @ProfileField(key = "ApplicationId", required = true, secure = false)
    private String applicationId;

    @Expose
    @SerializedName("ClientSecret")
    @ProfileField(key = "ClientSecret", required = true, secure = true)
    private String clientSecret;

    @Expose
    @SerializedName("GiteaUrl")
    @ProfileField(key = "GiteaUrl", required = true, secure = false)
    private String giteaUrl;

    @Expose
    @SerializedName("PersonalAccessToken")
    @ProfileField(key = "PersonalAccessToken", required = true, secure = true)
    private String personalAccessToken;
    private GiteaClient giteaClient;

    public GiteaConfiguration() {
    }

    public GiteaConfiguration(String applicationId, String clientSecret) {
        this(applicationId, clientSecret, null, "");
    }

    public GiteaConfiguration(String applicationId, String clientSecret, String giteaUrl, String personalAccessToken) {
        this.applicationId = applicationId;
        this.clientSecret = clientSecret;
        this.giteaUrl = giteaUrl;
        this.personalAccessToken = personalAccessToken;
    }

    public String personalAccessToken() {
        return personalAccessToken;
    }

    public String applicationId() {
        return applicationId;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public String giteaUrl() {
        return giteaUrl;
    }

    public String giteaBaseURL() {
        return giteaUrl;
    }

    public static GiteaConfiguration fromJSON(String json) {
        return GSON.fromJson(json, GiteaConfiguration.class);
    }

    public Map<String, String> toProperties() {
        return GSON.fromJson(toJSON(), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    public GiteaClient giteaClient() {
        if (giteaClient == null) {
            giteaClient = new GiteaClient(this);
        }

        return giteaClient;
    }
}
