
pipeline {
    agent any

    tools {
       maven 'Maven3.9.6'
       }
    stages {
       stage('code compile') {
           steps {
             echo "perform code compiling.."
             git 'https://github.com/sr691/ABC-project.git'
             sh 'mvn compile'

           }

        }
		
        stage('unit test') {
           steps { 
             echo "perform unit testing.."
             sh 'mvn test'
           }
		
           post {
             always {
              junit stdioRetention: '', testResults: 'target/surefire-reports/*.xml'
             }
           }
        }
		
        stage('package') {
           steps {
              echo "generate the war file"
              sh 'mvn package'
           }

           post {
             always {
               archiveArtifacts artifacts: 'target/*.war', followSymlinks: false
             }
           }
        }
		
		stage('build docker image') {
           steps {
              echo "build docker image using docker file"
              sh 'docker build --file Dockerfile --tag sharmi459/abc_tech:$BUILD_NUMBER .'
           }

        }
		
		stage('push docker image') {
	       steps {
	           withDockerRegistry(credentialsId: 'DOCKER_HUB_LOGIN', url: 'https://index.docker.io/v1/') {
               sh script: 'docker push docker.io/sharmi459/abc_tech:$BUILD_NUMBER'
                }
	           
	        }
		          
		}
		
		stage('docker deploy') {
	       steps {
	           echo "deploying the abc docker container into tomact server"
	           sh 'docker stop abc-container || true'
			   sh 'docker rm -f abc-container || true'
			   sh 'docker run -d -p 9090:8080 --name abc-container sharmi459/abc_tech:$BUILD_NUMBER'
			   #sh 'docker run -d -P --name abc-container sharmi459/abc_tech:$BUILD_NUMBER'
	        }
		
		}
	
    }

}
