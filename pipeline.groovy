pipeline {
    agent any
    environment {
        CONTAINER_NAME = "my-web-app" // Ganti dengan nama container Docker Anda yang sebenarnya
    }
    stages {
        stage('Checkout Code') {
            script {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'server_aplikasi_ssh', url: 'https://github.com/LaodeGhazy/project1.git']]]) // Kredensial Git di sini
            }
        }
        stage('Periksa Status Aplikasi') {
            script {
            	sh 'ls -l ./check_app_status.sh'
                sh './check_app_status.sh' // Pastikan path dan permission execute sudah benar
            }
            post {
                failure {
                    script {
                        sshagent(credentials: ['server_aplikasi_ssh']) { // Kredensial SSH di sini
                            sh "docker restart ${my-web-app}" // Gunakan variabel dari environment
                        }
                        // Email notifikasi (opsional)
                    }
                }
                success {
                    script {
                        // Email notifikasi (opsional)
                    }
                }
            }
        }
    }
}
