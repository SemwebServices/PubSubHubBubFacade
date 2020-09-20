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
      app_version = props.appVersion
      semantic_version_components = app_version.toString().split('\\.')
      is_snapshot = app_version.contains('SNAPSHOT')
      constructed_tag = "build-${props?.appVersion}-${checkout_details?.GIT_COMMIT?.take(12)}"
      do_k8s_update = false
      println("Got props: asString:${props} appVersion:${props.appVersion}/${props['appVersion']}/${semantic_version_components}");
      sh 'echo branch:$BRANCH_NAME'
      sh 'echo commit:$checkout_details.GIT_COMMIT'
    }

    stage ('build service assembly') {
      container('jdk11') {
        dir ('feedFacade') {
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
          println("Docker build")
          docker_image = docker.build("caphub_feedfacade")
        }
      }
    }

    stage('Publish Docker Image') {
      container('docker') {
        dir('docker') {
          if ( checkout_details?.GIT_BRANCH == 'master' ) {
            println("Considering build tag : ${constructed_tag} version:${props.appVersion}");
            // Some interesting stuff here https://github.com/jenkinsci/pipeline-examples/pull/83/files
            if ( !is_snapshot ) {
              do_k8s_update=true
              docker.withRegistry([credentialsId:'semwebdockerhub', url:'']) {
                println("Publishing released version with latest tag and semver ${semantic_version_components}");
                docker_image.push('latest')
                docker_image.push(app_version)
                docker_image.push("${semantic_version_components[0]}.${semantic_version_components[1]}".toString())
                docker_image.push(semantic_version_components[0])
              }
            }
            else {
              docker.withRegistry([credentialsId:'semwebdockerhub', url:'']) {
                println("Publishing snapshot-latest");
                docker_image.push('snapshot-latest')
              }
            }
          }
          else {
            println("Not publishing docker image for branch ${checkout_details?.GIT_BRANCH}. Please merge to master for a docker image build");
          }
        }
      }
    }
  }
}
