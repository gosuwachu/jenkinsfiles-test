// Option 4A: Trigger Jenkinsfile - orchestrates pipeline jobs
// Each job reads its own ci/*.Jenkinsfile
// Jobs are re-triggerable individually

pipeline {
    agent any

    stages {
        stage('Start') {
            steps {
                echo 'Starting Mobile CI/CD Pipeline (4A)...'
            }
        }

        stage('Build & Quality') {
            parallel {
                stage('iOS Build') {
                    steps {
                        build job: 'pipeline-4a/ios-build', wait: true
                    }
                }
                stage('Android Build') {
                    steps {
                        build job: 'pipeline-4a/android-build', wait: true
                    }
                }
                stage('iOS Tests') {
                    steps {
                        build job: 'pipeline-4a/ios-unit-tests', wait: true
                    }
                }
                stage('Android Tests') {
                    steps {
                        build job: 'pipeline-4a/android-unit-tests', wait: true
                    }
                }
                stage('iOS Lint') {
                    steps {
                        build job: 'pipeline-4a/ios-linter', wait: true
                    }
                }
                stage('Android Lint') {
                    steps {
                        build job: 'pipeline-4a/android-linter', wait: true
                    }
                }
            }
        }

        stage('Deploy') {
            parallel {
                stage('iOS Deploy') {
                    steps {
                        build job: 'pipeline-4a/ios-deploy', wait: true
                    }
                }
                stage('Android Deploy') {
                    steps {
                        build job: 'pipeline-4a/android-deploy', wait: true
                    }
                }
            }
        }
    }
}
