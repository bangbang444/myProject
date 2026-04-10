def IMAGE_NAME = 'bangbang444/gourmet-backend'
def DOCKER_HUB_ID = 'DockerHubGeneral'
def SSH_AGENT_ID = 'jasonscom_key'

node {
    properties([pipelineTriggers([githubPush()])])
    
    def jdk = tool name: 'jdk17'
    env.JAVA_HOME = jdk
    env.PATH = "${jdk}/bin:${env.PATH}"
    
    stage('Clone') {
        checkout scmGit(
            branches: [[name: 'develop']],
            extensions: [submodule(parentCredentials: true, trackingSubmodules: true)],
            userRemoteConfigs: [[credentialsId: "GitHubGeneral", url: 'https://github.com/bangbang444/myProject']]
        )
    }

    stage('Test'){
        sh 'chmod +x gradlew'
        withCredentials([file(credentialsId: 'TEST_YML', variable: 'TEST_YML_PATH')]){
            sh 'mkdir -p src/test/resources'
            sh 'cp $TEST_YML_PATH src/test/resources/application.yml'
            sh './gradlew clean test'
        }
    }
    
    stage('Build') {
        withCredentials([
            file(credentialsId: 'PROD_YML', variable: 'PROD_YML_PATH')
        ]) {
            sh 'mkdir -p src/main/resources'
            sh 'cp $PROD_YML_PATH src/main/resources/application-prod.yml'
            sh './gradlew build -Dspring.profiles.active=prod -x test'
        }
    }
    
    stage('Docker Build') {
        app = docker.build("${IMAGE_NAME}", '--no-cache .')
    }
     
    stage('Push Image') {
        docker.withRegistry('https://registry.hub.docker.com', "${DOCKER_HUB_ID}") {
            app.push("${env.BUILD_NUMBER}")
            app.push("latest")
        }
    }
     
    stage('Deploy') {
        withCredentials([
            usernamePassword(credentialsId: 'DockerHubGeneral', passwordVariable: 'TOKEN_VALUE', usernameVariable: 'DOCKER_USER'),
            string(credentialsId: 'SSH_PORT', variable: 'TARGET_PORT'),
            string(credentialsId: 'SSH_HOST', variable: 'TARGET_HOST'),
            string(credentialsId: 'SSH_USER', variable: 'TARGET_USER')
        ]) {
            sshagent(credentials: ["${SSH_AGENT_ID}"]) {
                sh """
                ssh -p "${TARGET_PORT}" -o StrictHostKeyChecking=no "${TARGET_USER}"@"${TARGET_HOST}" << 'EOF'
                    echo "${TOKEN_VALUE}" | sudo docker login -u "${DOCKER_USER}" --password-stdin
                    sudo /usr/bin/sh "/home/${TARGET_USER}/gourmet/dev_deploy.sh"
                    exit
                EOF
                """
            }
        }
    }
}
