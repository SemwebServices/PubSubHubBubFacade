2021-01-30 Open 2.1.22-SNAPSHOT

  * The cleanup background will now also expunge any feed issues older than the rolling wall period

2021-01-30 Release 2.1.21

  * Add GIT build environment variables to Jenkinsfile
  * Add a number of indexes to improve index page load time
  * Added this ChangeLog

2021-01-30 Release 2.1.20

  * Remove last remaining use of Url.openStream in RSS/ATOM fetch.
  * Fix issue where feed fetched a second time in rapid succession - reuse the first fetch
