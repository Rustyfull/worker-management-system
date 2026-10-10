pipeline{

    agent { label 'Jenkins-Agent' }
    tools {
        jdk 'Java21'
        maven 'Maven3'

    }

    stages{

        stage("Cleanup Workspace"){
            steps{
                cleanWs()
            }
        }


        stage("Checkout from SCM"){
            steps{
                git branch: 'main', credentialsId: 'github', url:'https://github.com/Rustyfull/worker-management-system'
            }
        }

        stage("Build Application"){
            steps{
                sh "mvn clean compile"
            }
        }


        stage("Test Application"){
            steps{
                catchError(buildResult: 'SUCCESS', stageResult:'ERROR'){
                    sh "mvn test"
                }

            }
        }


        stage("SonarQube Analysis"){
            steps{
                script{
                    withSonarQubeEnv(credentialsId: 'sonarqube'){
                        sh "mvn sonar:sonar"

                    }
                }

            }
        }

    }

}