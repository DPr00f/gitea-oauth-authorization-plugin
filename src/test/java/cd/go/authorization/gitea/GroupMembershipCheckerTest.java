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
import cd.go.authorization.gitea.client.models.GiteaGroup;
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

public class GroupMembershipCheckerTest {

    @Mock
    private GiteaUser giteaUser;
    @Mock
    private TokenInfo tokenInfo;
    @Mock
    private GiteaClient giteaClient;
    private GroupMembershipChecker groupMembershipChecker;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        groupMembershipChecker = new GroupMembershipChecker();
    }

    @Test
    public void shouldReturnTrueWhenUserIsAMemberOfGroup() throws Exception {
        final Map<String, List<String>> groupsFromRole = singletonMap("group-a", emptyList());
        final GiteaGroup giteaGroup = mock(GiteaGroup.class);
        final List<GiteaGroup> giteaGroups = asList(giteaGroup);
        final String personalAccessToken = "some-random-token";

        when(giteaGroup.getName()).thenReturn("group-a");

        final boolean member = groupMembershipChecker.memberOfAtLeastOneGroup(giteaUser, personalAccessToken, giteaClient, giteaGroups, groupsFromRole);

        assertTrue(member);
        verifyNoMoreInteractions(giteaClient);
    }

    @Test
    public void shouldCheckForAccessLevelWhenProvidedInRoleConfig() throws Exception {
        final Map<String, List<String>> groupsFromRole = singletonMap("group-a", asList("developer"));
        final GiteaGroup giteaGroupA = mock(GiteaGroup.class);
        final GiteaGroup giteaGroupB = mock(GiteaGroup.class);
        final List<GiteaGroup> giteaGroups = asList(giteaGroupB, giteaGroupA);
        final MembershipInfo membershipInfo = mock(MembershipInfo.class);
        final String personalAccessToken = "some-random-token";

        when(giteaGroupA.getName()).thenReturn("group-a");
        when(giteaGroupB.getName()).thenReturn("group-b");
        when(giteaClient.groupMembershipInfo(personalAccessToken, giteaGroupA.getId(), giteaUser.getId())).thenReturn(membershipInfo);
        when(membershipInfo.getAccessLevel()).thenReturn(AccessLevel.DEVELOPER);

        final boolean member = groupMembershipChecker.memberOfAtLeastOneGroup(giteaUser, personalAccessToken, giteaClient, giteaGroups, groupsFromRole);

        assertTrue(member);
    }

}
