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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class GitLabRoleConfigurationTest {

    @Test
    public void shouldDeserializeRoleConfig() throws Exception {
        final GitLabRoleConfiguration gitLabRoleConfiguration = GitLabRoleConfiguration.fromJSON("{\n" +
                "  \"Groups\": \"group-1: guest, owner\",\n" +
                "  \"Projects\": \"project-1:developer\"" +
                "}");

        assertThat(gitLabRoleConfiguration.groups(), hasEntry("group-1", asList("guest", "owner")));
    }
}
