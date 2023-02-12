pipeline {
    agent {
        node {
            label 'docker-agent-maven'
        }
    }
    triggers {
        pollSCM '* * * * *'
    }
    stages {
        stage('Prepare') {
            steps {
                echo "Testing tools versions.."
                sh 'java --version'
                sh 'mvn --version'
                sh 'docker --version'
            }
        }
        stage('Build') {
            steps {
                echo "Building.."
                sh 'mvn package -Dmaven.test.skip'
            }
        }
        stage('Test') {
            steps {
                echo "Testing.."
                sh 'mvn test'
            }
        }
        stage('Pre-Deploy'){
            steps {
                echo "Pushing images.."
                sh 'mvn jib:build'
            }
        }
        stage('Deploy'){
            steps {
                echo "Deploying docker-compose services.."
                sh 'docker-compose up -d'
            }
        }
    }
    post {
        always {
            echo 'Pipeline has completed'
        }
        success {
            echo 'Deploy is successfull'
        }
        failure {
            echo 'Deploy is failed'
        }
    }
}