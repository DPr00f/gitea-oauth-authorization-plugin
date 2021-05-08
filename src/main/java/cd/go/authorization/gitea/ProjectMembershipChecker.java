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
import cd.go.authorization.gitea.client.models.GiteaProject;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.client.models.MembershipInfo;
import cd.go.authorization.gitea.models.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cd.go.authorization.gitea.GiteaPlugin.LOG;
import static java.text.MessageFormat.format;

public class ProjectMembershipChecker {

    public boolean memberOfAtLeastOneProject(GiteaUser giteaUser, String personalAccessToken, GiteaClient giteaClient, List<GiteaProject> projectsFromGiteaForAUser, Map<String, List<String>> projectsFromRole) throws IOException {
        final List<GiteaProject> matchingProjects = filterGroupBasedOnRoleConfiguration(projectsFromGiteaForAUser, projectsFromRole);

        for (GiteaProject giteaProject : matchingProjects) {
            final List<String> accessLevels = projectsFromRole.get(giteaProject.getName());

            if (accessLevels == null || accessLevels.isEmpty()) {
                LOG.info(format("User `{0}` is member of `{1}` project.", giteaUser.getUsername(), giteaProject.getName()));
                return true;
            }

            final MembershipInfo membershipInfo = giteaClient.projectMembershipInfo(personalAccessToken, giteaProject.getId(), giteaUser.getId());

            if (membershipInfo.getAccessLevel() != null && accessLevels.contains(membershipInfo.getAccessLevel().toString().toLowerCase())) {
                LOG.info(format("User `{0}` is member of `{1}` project with access level `{2}`.", giteaUser.getUsername(), giteaProject.getName(), membershipInfo.getAccessLevel()));
                return true;
            }
        }
        return false;
    }

    private List<GiteaProject> filterGroupBasedOnRoleConfiguration(List<GiteaProject> projectsFromGitea, Map<String, List<String>> projectsFromRole) throws IOException {
        final List<GiteaProject> giteaProjects = new ArrayList<>();
        for (GiteaProject projectFromGitea : projectsFromGitea) {
            if (projectsFromRole.containsKey(projectFromGitea.getName())) {
                giteaProjects.add(projectFromGitea);
            }
        }
        return giteaProjects;
    }
}
