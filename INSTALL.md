# Gitea OAuth authorization plugin for GoCD

## Requirements

* GoCD server version v17.5.0 or above
* Gitea OAuth application's `ApplicationId` and `ClientSectret`

## Installation

Copy the file `build/libs/gitea-authorization-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external`
and restart the server. The `GO_SERVER_DIR` is usually `/var/lib/go-server` on Linux and `C:\Program Files\Go Server`
on Windows.

## Configuration

###  Create Gitea OAuth application

1. Login into your Gitea/Gitea Enterprise account
2. Navigate to **_Settings_**
!["Gitea settings"][1]

3. Click on **Applications**.
!["Gitea applications"][2]

4. Fill the following details for application
    - Give a name to your application
    - In `Redirect URI`, specify `https://your.goserver.url/go/plugin/cd.go.authorization.gitea/authenticate`.
    - In scopes, select `api`

    !["Fill application details"][3]

5. Click **Save application**.
!["Save application"][4]

7. Note down the `Application ID` and `Secret` of your application.
!["Gitea application info"][5]

### Create Personal Access Token

1. Login into your Gitea/Gitea Enterprise account
2. Navigate to **_Settings_**
!["Gitea settings"][1]

3. Click on **Access Tokens**
!["Gitea access tokens"][7]

4. Fill the following details for access token
    - Give a name to your token
    - In scopes, select `api` and `read_user`

    !["Fill access token detail"][8]

5. Click **Save token**.
!["Save token"][9]

6. Note down the `Token Value`.
![Gitea personal access token info][10]

### Create Authorization Configuration

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Security_** _>_ **_Authorization Configuration_**
2. Click on **_Add_** to create new authorization configuration
    1. Specify `id` for auth config
    2. Select `Gitea OAuth authorization plugin` for **_Plugin id_**
    3. Choose `Gitea` or `Gitea Enterprise` for `Authenticate with`.
    4. Specify **_Application ID_** and **_Client Secret_**
    5. Specify **_Token Value_**
    6. Save your configuration

    ![Create authorization configuration][6]

[1]: images/nav_settings.png    "Gitea settings"
[2]: images/nav_applications.png    "Gitea applications"
[3]: images/fill_application_details.png   "Fill application details"
[4]: images/save_application.png   "Save application"
[5]: images/application_info.png   "Gitea application info"
[6]: images/gocd_auth_config.gif  "Create authorization configuration"
[7]: images/nav_access_tokens.png "Gitea Access Tokens"
[8]: images/fill_access_token_details.png "Fill access token details"
[9]: images/save_token.png "Save token"
[10]: images/token_info.png "Gitea personal accesss token info"
