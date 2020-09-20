#!groovy

podTemplate(
  containers:[
    containerTemplate(name: 'jdk11',                image:'adoptopenjdk:11-jdk-openj9',   ttyEnabled:true, command:'cat'),
    containerTemplate(name: 'docker',               image:'docker:18',                    ttyEnabled:true, command:'cat')
  ],
  volumes: [
    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
  ])
{
  node(POD_LABEL) {

    stage ('checkout') {
      checkout_details = checkout scm
      props = readProperties file: './feedFacade/gradle.properties'
      println("Got props ${props}");
      do_k8s_update = false
      sh 'echo branch:$BRANCH_NAME'
      sh 'echo commit:$checkout_details.GIT_COMMIT'
    }

    stage ('build service assembly') {
      container('jdk11') {
        dir ('feedFacade') {
          String[] semantic_version_components = props.appVersion.toString().split('\\.')
          println("Got props: asString:${props} appVersion:${props.appVersion}/${props['appVersion']}/${semantic_version_components}");

          sh './gradlew --no-daemon -x test -x integrationTest --console=plain clean build'
          sh 'ls -la ./build/libs/*'
          sh "cp build/libs/feedFacade-${props.appVersion}.war ../docker/feedFacade.war".toString()
        }
      }
    }

    // https://www.jenkins.io/doc/book/pipeline/docker/
    stage('Build Docker Image') {
      container('docker') {
        dir('docker') {
          println("Docker build props: asString:${props} appVersion:${props.appVersion}/${props['appVersion']}");
          docker_image = docker.build("caphub_feedfacade")
        }
      }
    }

  }
}
