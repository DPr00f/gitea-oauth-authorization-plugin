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
import cd.go.authorization.gitea.client.models.AccessLevel;
import cd.go.authorization.gitea.client.models.GiteaProject;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.client.models.MembershipInfo;
import cd.go.authorization.gitea.models.TokenInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProjectMembershipCheckerTest {
    public static final String PERSONAL_ACCESS_TOKEN = "some-access-token";
    @Mock
    private GiteaUser giteaUser;
    @Mock
    private TokenInfo tokenInfo;
    @Mock
    private GiteaClient giteaClient;
    private ProjectMembershipChecker projectMembershipChecker;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        projectMembershipChecker = new ProjectMembershipChecker();
    }

    @Test
    public void shouldReturnTrueWhenUserIsAMemberOfGroup() throws Exception {
        final Map<String, List<String>> groupsFromRole = singletonMap("project-duck-simulator", emptyList());
        final GiteaProject giteaProject = mock(GiteaProject.class);
        final List<GiteaProject> giteaProjects = asList(giteaProject);

        when(giteaProject.getName()).thenReturn("project-duck-simulator");

        final boolean member = projectMembershipChecker.memberOfAtLeastOneProject(giteaUser, PERSONAL_ACCESS_TOKEN, giteaClient, giteaProjects, groupsFromRole);

        assertTrue(member);
        verifyNoMoreInteractions(giteaClient);
    }

    @Test
    public void shouldCheckForAccessLevelWhenProvidedInRoleConfig() throws Exception {
        final Map<String, List<String>> groupsFromRole = singletonMap("project-duck-simulator", asList("developer"));
        final GiteaProject giteaProjectA = mock(GiteaProject.class);
        final GiteaProject giteaProjectB = mock(GiteaProject.class);
        final List<GiteaProject> giteaProjects = asList(giteaProjectB, giteaProjectA);
        final MembershipInfo membershipInfo = mock(MembershipInfo.class);

        when(giteaProjectA.getName()).thenReturn("project-duck-simulator");
        when(giteaProjectB.getName()).thenReturn("project-sudoku-solver");
        when(giteaClient.projectMembershipInfo(PERSONAL_ACCESS_TOKEN, giteaProjectA.getId(), giteaUser.getId())).thenReturn(membershipInfo);
        when(membershipInfo.getAccessLevel()).thenReturn(AccessLevel.DEVELOPER);

        final boolean member = projectMembershipChecker.memberOfAtLeastOneProject(giteaUser, PERSONAL_ACCESS_TOKEN, giteaClient, giteaProjects, groupsFromRole);

        assertTrue(member);
    }

}
