pipeline {
    agent any
    tools {
        maven 'maven3.9.6'
    }
    stages {
      stage('compile') {
	        steps {
		            git url: 'https://github.com/lerndevopswithdurga/ABC_Technologies.git'
		            sh script: '/opt/maven/bin/mvn compile'
          }
      }
      
      stage('unit-test') {
	        steps {
	                sh script: '/opt/maven/bin/mvn test'
            }
	        post {
            success {
                    junit 'target/surefire-reports/*.xml'
            }
            }			
      }
      
      stage('package') {
	        steps {
		            sh script: '/opt/maven/bin/mvn package'	
            }		
      }
      
      stage('ansible-dockerbuild-push') {
	        steps {
                    echo "building image and pushing to dockerhub..."
	                withDockerRegistry(credentialsId: 'DOCKER_HUB_LOGIN', url: 'https://index.docker.io/v1/') {
                    sh 'ansible-playbook -i localhost, deploy/dockerbuild-push.yml'

                    }
	           
	        }
	  }
      stage('Deploy to K8s') {
  	    steps {
    		    sh 'sed -i "s/bno/"$BUILD_NUMBER"/g" deploy/k8s_deploy.yml'
    		    sh 'kubectl apply -f deploy/k8s_deploy.yml'
	      }
	    }   
    }
}		
