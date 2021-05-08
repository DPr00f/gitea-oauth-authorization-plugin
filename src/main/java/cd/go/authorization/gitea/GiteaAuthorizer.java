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
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.GiteaRole;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cd.go.authorization.gitea.GiteaPlugin.LOG;
import static java.util.stream.Collectors.toList;

public class GiteaAuthorizer {
    private final GroupMembershipChecker groupMembershipChecker;
    private final ProjectMembershipChecker projectMembershipChecker;

    public GiteaAuthorizer() {
        this(new GroupMembershipChecker(), new ProjectMembershipChecker());
    }

    GiteaAuthorizer(GroupMembershipChecker groupMembershipChecker, ProjectMembershipChecker projectMembershipChecker) {
        this.groupMembershipChecker = groupMembershipChecker;
        this.projectMembershipChecker = projectMembershipChecker;
    }

    public List<String> authorize(GiteaUser giteaUser, AuthConfig authConfig, List<GiteaRole> roles) throws IOException {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> assignedRoles = checkIfUserIsWhiteListed(giteaUser.getUsername(), roles);

        final List<GiteaRole> remainingRoles = roles.stream().filter(role -> !assignedRoles.contains(role.name())).collect(toList());

        if (remainingRoles.isEmpty()) {
            LOG.debug("No more roles to check.");
            return assignedRoles;
        }

        final GiteaClient giteaClient = authConfig.giteaConfiguration().giteaClient();
        String personalAccessToken = authConfig.giteaConfiguration().personalAccessToken();
        final List<GiteaGroup> groupsFromGitea = giteaClient.groups(personalAccessToken);
        final List<GiteaProject> projectsFromGitea = giteaClient.projects(personalAccessToken);

        for (GiteaRole role : remainingRoles) {
            final Map<String, List<String>> groupsFromRole = role.roleConfiguration().groups();

            if (groupMembershipChecker.memberOfAtLeastOneGroup(giteaUser, personalAccessToken, giteaClient, groupsFromGitea, groupsFromRole)) {
                assignedRoles.add(role.name());
                continue;
            }

            final Map<String, List<String>> projectsFromRole = role.roleConfiguration().projects();

            if (projectMembershipChecker.memberOfAtLeastOneProject(giteaUser, personalAccessToken, giteaClient, projectsFromGitea, projectsFromRole)) {
                assignedRoles.add(role.name());
            }
        }

        return assignedRoles;
    }

    private List<String> checkIfUserIsWhiteListed(String username, List<GiteaRole> roles) {
        return roles.stream().filter(role -> role.roleConfiguration().users().contains(username))
                .map(role -> role.name())
                .collect(toList());
    }
}
