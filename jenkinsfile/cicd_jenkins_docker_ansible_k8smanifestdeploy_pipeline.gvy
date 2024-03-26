pipeline {
    agent any
    tools {
        maven 'maven3.9.6'
    }
    stages {
      stage('compile') {
	        steps {
		            git url: 'https://github.com/sr691/ABC-project.git.git'
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
                    sh 'ansible-playbook -i localhost, deploy/dockerbuildimage_push.yml'

                    }
	           
	        }
	  }
      stage('ansible-k8sdeploy-qa') {
	    steps {
		    sh 'ansible-playbook --inventory /etc/ansible/hosts deploy/ansibleplaybook-k8smanifestdeploy.yml'
	    }
      }
    }
}		
