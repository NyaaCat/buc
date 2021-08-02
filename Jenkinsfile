pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "jdk11"
            }
            steps {
                sh 'gradle build'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}
