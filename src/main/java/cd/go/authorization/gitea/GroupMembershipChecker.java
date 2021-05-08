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
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.client.models.MembershipInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cd.go.authorization.gitea.GiteaPlugin.LOG;
import static java.text.MessageFormat.format;

public class GroupMembershipChecker {

    public boolean memberOfAtLeastOneGroup(GiteaUser giteaUser, String personalAccessToken, GiteaClient giteaClient, List<GiteaGroup> groupsFromGiteaForAUser, Map<String, List<String>> groupsFromRole) throws IOException {
        final List<GiteaGroup> matchingGroups = filterGroupBasedOnRoleConfiguration(groupsFromGiteaForAUser, groupsFromRole);

        for (GiteaGroup giteaGroup : matchingGroups) {
            final List<String> accessLevels = groupsFromRole.get(giteaGroup.getName());

            if (accessLevels == null || accessLevels.isEmpty()) {
                LOG.info(format("User `{0}` is member of `{1}` group.", giteaUser.getUsername(), giteaGroup.getName()));
                return true;
            }

            final MembershipInfo membershipInfo = giteaClient.groupMembershipInfo(personalAccessToken, giteaGroup.getId(), giteaUser.getId());

            if (membershipInfo.getAccessLevel() != null && accessLevels.contains(membershipInfo.getAccessLevel().toString().toLowerCase())) {
                LOG.info(format("User `{0}` is member of `{1}` group with access level `{2}`.", giteaUser.getUsername(), giteaGroup.getName(), membershipInfo.getAccessLevel()));
                return true;
            }
        }
        return false;
    }

    private List<GiteaGroup> filterGroupBasedOnRoleConfiguration(List<GiteaGroup> groupsFromGitea, Map<String, List<String>> groupsFromRole) throws IOException {
        final List<GiteaGroup> giteaGroups = new ArrayList<>();
        for (GiteaGroup groupFromGitea : groupsFromGitea) {
            if (groupsFromRole.containsKey(groupFromGitea.getName())) {
                giteaGroups.add(groupFromGitea);
            }
        }
        return giteaGroups;
    }
}
