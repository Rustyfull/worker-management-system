pipeline{

    agent { label 'Jenkins-Agent' }
    tools {
        jdk 'Java21'
        maven 'Maven3'

    }

    environment{
        APP_NAME = "worker-management-system"
        RELEASE = "1.0.0"
        DOCKER_USER = "pkatjs"
        DOCKER_PASS = "dockerhub"
        IMAGE_NAME = "${DOCKER_USER}" + "/" + "${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
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
                catchError(buildResult: 'SUCCESS', stageResult:'FAILURE'){
                    sh "mvn test"
                }

            }
        }


        stage("SonarQube Analysis"){
            steps{
                script{
                    withSonarQubeEnv(credentialsId: 'sonarqube'){
                        sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar"

                    }
                }

            }
        }



        stage("Quality Gate"){
            steps{
                script{
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonarqube'
                }
            }
        }


        stage("Build & Push Docker Image"){
            steps{
                script{
                    docker.withRegistry("",DOCKER_PASS){
                        docker_image = docker.build "${IMAGE_NAME}"
                    }

                    docker.withRegistry("", DOCKER_PASS){
                        docker_image.push("${IMAGE_TAG}")
                        docker_image.push("latest")
                    }
                }
            }
        }



        stage('Trivy Scan') {
            steps {
                sh '''
            docker run --rm \
              -v /var/run/docker.sock:/var/run/docker.sock \
              aquasec/trivy:latest \
              image --no-progress \
              --scanners vuln \
              pkatjs/worker-management-system:${IMAGE_TAG}
        '''
            }
        }



        stage('Cleanup Artifacts'){
            steps{
                script{
                    sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker rmi ${IMAGE_NAME}:latest"
                }
            }
        }

    }

}