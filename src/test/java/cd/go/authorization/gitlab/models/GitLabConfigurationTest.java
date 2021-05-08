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

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class GitLabConfigurationTest {

    @Test
    public void shouldDeserializeGitLabConfiguration() throws Exception {
        final GitLabConfiguration gitLabConfiguration = GitLabConfiguration.fromJSON("{\n" +
                "  \"ApplicationId\": \"client-id\",\n" +
                "  \"GiteaUrl\": \"https://enterprise.url\",\n" +
                "  \"ClientSecret\": \"client-secret\"" +
                "}");

        assertThat(gitLabConfiguration.applicationId(), is("client-id"));
        assertThat(gitLabConfiguration.clientSecret(), is("client-secret"));
        assertThat(gitLabConfiguration.giteaUrl(), is("https://enterprise.url"));
    }

    @Test
    public void shouldSerializeToJSON() throws Exception {
        GitLabConfiguration gitLabConfiguration = new GitLabConfiguration("client-id", "client-secret", "http://enterprise.url", "some-random-token");

        String expectedJSON = "{\n" +
                "  \"ApplicationId\": \"client-id\",\n" +
                "  \"ClientSecret\": \"client-secret\",\n" +
                "  \"GiteaUrl\": \"http://enterprise.url\",\n" +
                "  \"PersonalAccessToken\":\"some-random-token\"" +
                "}";

        JSONAssert.assertEquals(expectedJSON, gitLabConfiguration.toJSON(), true);

    }

    @Test
    public void shouldConvertConfigurationToProperties() throws Exception {
        GitLabConfiguration gitLabConfiguration = new GitLabConfiguration("client-id", "client-secret", "http://enterprise.url", "some-random-token");

        final Map<String, String> properties = gitLabConfiguration.toProperties();

        assertThat(properties, hasEntry("ApplicationId", "client-id"));
        assertThat(properties, hasEntry("ClientSecret", "client-secret"));
        assertThat(properties, hasEntry("GiteaUrl", "http://enterprise.url"));
    }
}
