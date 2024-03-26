pipeline {
    agent any

    tools {
        maven 'Maven3.9.6'
    }
    stages {
        stage('ABC_CodeCompile') {
           steps {
             echo "compiling..."
             git 'https://github.com/sr691/ABC-project.git'
             sh 'mvn compile'

           }

        }

        stage('ABC_Unittest') {
           steps { 
             echo "testing..."
             sh 'mvn test'
           }

           post {
             always {
              junit stdioRetention: '', testResults: 'target/surefire-reports/*.xml'
             }
           }
        }

        stage('ABC_Package') {
           steps {
              echo "packaging..."
              sh 'mvn package'
           }

           post {
             always {
               archiveArtifacts artifacts: 'target/*.war', followSymlinks: false
             }
           }
        }

        stage('docker build ') {
	         steps {
              echo "docker building image..."
	      echo '$WORKSPACE'
	      sh 'ls -la $WORKSPACE'
              sh 'cd $WORKSPACE'
	          sh 'docker build --file Dockerfile --tag sharmi459/abc_tech:$BUILD_NUMBER .'
	          sh script: 'ansible-playbook -i localhost, deploy/dockerbuild-push.yml'
           }	
        }
        stage('push docker image') {
	        steps {
              echo "pushing image to docker hub..."
	          withDockerRegistry(credentialsId: 'DOCKER_HUB_LOGIN', url: 'https://index.docker.io/v1/') {
              sh 'docker push docker.io/sharmi459/abc_tech:$BUILD_NUMBER'
                }
	           
	        }
		    }
        stage('docker deploy') {
          steps {
            echo "deploying to docker container..."
            sh 'docker stop abc-container || true' 
            sh 'docker rm -f abc-container || true' 
            sh 'docker run -d -P --name abc-container sharmi459/abc_tech:$BUILD_NUMBER'
            sh 'docker ps -a'
          }
        }
    }
}
