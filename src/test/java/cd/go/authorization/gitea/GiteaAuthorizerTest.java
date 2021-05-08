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

import cd.go.authorization.gitea.client.GiteaClient;
import cd.go.authorization.gitea.client.models.GiteaGroup;
import cd.go.authorization.gitea.client.models.GiteaProject;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.models.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GiteaAuthorizerTest {
    private GroupMembershipChecker groupMembershipChecker;
    private GiteaUser giteaUser;
    private TokenInfo tokenInfo;
    private AuthConfig authConfig;
    private GiteaAuthorizer giteaAuthorizer;
    private GiteaConfiguration giteaConfiguration;
    private GiteaClient giteaClient;
    private ProjectMembershipChecker projectMembershipChecker;

    @Before
    public void setUp() throws Exception {
        groupMembershipChecker = mock(GroupMembershipChecker.class);
        giteaUser = mock(GiteaUser.class);
        tokenInfo = mock(TokenInfo.class);
        authConfig = mock(AuthConfig.class);
        giteaConfiguration = mock(GiteaConfiguration.class);
        giteaClient = mock(GiteaClient.class);
        projectMembershipChecker = mock(ProjectMembershipChecker.class);

        when(authConfig.giteaConfiguration()).thenReturn(giteaConfiguration);
        when(giteaConfiguration.giteaClient()).thenReturn(giteaClient);

        giteaAuthorizer = new GiteaAuthorizer(groupMembershipChecker, projectMembershipChecker);
    }

    @Test
    public void shouldAssignRoleWhenUsernameIsWhiteListed() throws Exception {
        final GiteaRole giteaRole = mock(GiteaRole.class);
        final GiteaRoleConfiguration giteaRoleConfiguration = mock(GiteaRoleConfiguration.class);

        when(giteaRole.roleConfiguration()).thenReturn(giteaRoleConfiguration);
        when(giteaRoleConfiguration.users()).thenReturn(asList("bob"));
        when(giteaUser.getUsername()).thenReturn("bob");
        when(giteaRole.name()).thenReturn("admin");

        final List<String> roles = giteaAuthorizer.authorize(giteaUser, authConfig, asList(giteaRole));

        assertThat(roles, hasSize(1));
        assertThat(roles, contains("admin"));
    }

    @Test
    public void shouldAssignRoleWhenUserIsAMemberOfAGroup() throws Exception {
        final GiteaRole giteaRole = mock(GiteaRole.class);
        final GiteaRoleConfiguration giteaRoleConfiguration = mock(GiteaRoleConfiguration.class);
        final List<GiteaGroup> giteaGroups = asList(mock(GiteaGroup.class));
        final Map<String, List<String>> groups = singletonMap("group-a", emptyList());
        final String personalAccessToken = "some-random-token";

        when(giteaClient.groups(personalAccessToken)).thenReturn(giteaGroups);
        when(giteaRole.name()).thenReturn("admin");
        when(giteaRole.roleConfiguration()).thenReturn(giteaRoleConfiguration);
        when(authConfig.giteaConfiguration().personalAccessToken()).thenReturn(personalAccessToken);
        when(giteaRoleConfiguration.groups()).thenReturn(groups);
        when(groupMembershipChecker.memberOfAtLeastOneGroup(giteaUser, personalAccessToken, giteaClient, giteaGroups, groups)).thenReturn(true);

        final List<String> roles = giteaAuthorizer.authorize(giteaUser, authConfig, asList(giteaRole));

        assertThat(roles, hasSize(1));
        assertThat(roles, contains("admin"));
    }

    @Test
    public void shouldAssignRoleWhenUserIsAMemberOfAProject() throws Exception {
        final GiteaRole giteaRole = mock(GiteaRole.class);
        final GiteaRoleConfiguration giteaRoleConfiguration = mock(GiteaRoleConfiguration.class);
        final List<GiteaGroup> giteaGroups = asList(mock(GiteaGroup.class));
        final List<GiteaProject> giteaProjects = asList(mock(GiteaProject.class));
        final Map<String, List<String>> projects = singletonMap("project-foo", emptyList());
        final String personalAccessToken = "some-random-token";

        when(giteaClient.projects(personalAccessToken)).thenReturn(giteaProjects);
        when(giteaRole.name()).thenReturn("admin");
        when(giteaRole.roleConfiguration()).thenReturn(giteaRoleConfiguration);
        when(authConfig.giteaConfiguration().personalAccessToken()).thenReturn(personalAccessToken);
        when(giteaRoleConfiguration.projects()).thenReturn(projects);
        when(groupMembershipChecker.memberOfAtLeastOneGroup(giteaUser, personalAccessToken, giteaClient, giteaGroups, giteaRoleConfiguration.groups())).thenReturn(false);

        when(projectMembershipChecker.memberOfAtLeastOneProject(giteaUser, personalAccessToken, giteaClient, giteaProjects, projects)).thenReturn(true);

        final List<String> roles = giteaAuthorizer.authorize(giteaUser, authConfig, asList(giteaRole));

        verify(projectMembershipChecker).memberOfAtLeastOneProject(any(), anyString(), any(), anyList(), anyMap());
        assertThat(roles, hasSize(1));
        assertThat(roles, contains("admin"));
    }

}
