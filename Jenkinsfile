pipeline {
    agent any

    environment {
        REGISTRY    = '192.168.45.91:31114'
        IMAGE       = 'parking-admin-dashboard'
        TAG         = "${BUILD_NUMBER}"
        GIT_REPO    = 'https://github.com/ubibio/parking-admin-dashboard.git'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh """
                    docker build -t ${REGISTRY}/${IMAGE}:${TAG} .
                    docker tag  ${REGISTRY}/${IMAGE}:${TAG} ${REGISTRY}/${IMAGE}:latest
                    docker push ${REGISTRY}/${IMAGE}:${TAG}
                    docker push ${REGISTRY}/${IMAGE}:latest
                """
            }
        }

        stage('Update K8s Manifest') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-pat', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                        sed -i 's|${REGISTRY}/${IMAGE}:.*|${REGISTRY}/${IMAGE}:${TAG}|g' k8s/deployment.yaml
                        git config user.email "jenkins@ci.local"
                        git config user.name  "Jenkins"
                        git remote set-url origin https://\${GIT_USER}:\${GIT_TOKEN}@github.com/ubibio/parking-admin-dashboard.git
                        git add k8s/deployment.yaml
                        git diff --cached --quiet || git commit -m "ci: update image tag to ${TAG}"
                        git pull --rebase --autostash origin main
                        git push origin HEAD:main
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ 배포 완료: http://192.168.45.91:30091"
        }
        failure {
            echo "❌ 빌드 실패 — Jenkins 로그를 확인하세요"
        }
    }
}
