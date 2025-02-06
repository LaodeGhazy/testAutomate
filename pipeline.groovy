pipeline {
    agent any
    environment {
        CONTAINER_NAME = "angry_mayer" // Nama container dari hasil docker ps
        IMAGE_NAME = "my-web-app" // Ganti dengan nama image yang sesuai
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'server_aplikasi_ssh', url: 'https://github.com/LaodeGhazy/testAutomate.git']]])
            }
        }
        stage('Periksa Status Aplikasi') {
            steps {
                sh 'ls -l ./check_app_status.sh'
                sh './check_app_status.sh' // Pastikan path dan permission execute sudah benar
            }
            post {
                failure {
                    script {
                        sshagent(credentials: ['server_aplikasi_ssh']) {
                            // Cek apakah container sedang berjalan atau tidak
                            sh """
                                if ! docker ps --filter name=${CONTAINER_NAME} --filter status=running | grep ${CONTAINER_NAME}; then
                                    echo "Container tidak berjalan, mencoba untuk memulai ulang..."
                                    docker run -d -p 5000:5000 ${IMAGE_NAME} // Memulai container baru jika container tidak berjalan
                                else
                                    echo "Container sudah berjalan, mencoba untuk merestart..."
                                    docker restart ${CONTAINER_NAME} // Merestart container yang ada
                                fi
                            """
                            // Email notifikasi (opsional) - Notifikasi jika build gagal
                            emailext (
                                subject: "Jenkins Build Gagal",
                                body: """
                                    Build Jenkins Anda gagal.
                                    Stage: Periksa Status Aplikasi
                                    Status Code: ${env.STATUS_CODE}
                                    Detail: ... (tambahkan detail lain yang relevan)
                                """,
                                recipientProviders: [[$class: 'DevelopersRecipientProvider']]

                            )
                        }
                    }
                }
                success {
                    script {
                        // Email notifikasi (opsional) - Notifikasi jika build berhasil
                        emailext (
                            subject: "Jenkins Build Berhasil",
                            body: "Build Jenkins Anda berhasil.",
                            to: "ghazylaode002@gmail.com"
                        )
                    }
                }
            }
        }
    }
}

