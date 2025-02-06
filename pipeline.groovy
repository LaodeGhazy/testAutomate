pipeline {
    agent any
    environment {
        CONTAINER_NAME = "dreamy_shockley" // Ganti dengan nama container Docker Anda yang sebenarnya
    }
    stages {
        stage('Checkout Code') {
            steps { // Gunakan blok 'steps' di sini
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'server_aplikasi_ssh', url: 'https://github.com/LaodeGhazy/testAutomate.git']]])
            }
        }
        stage('Periksa Status Aplikasi') {
            steps { // Gunakan blok 'steps' di sini
                sh 'ls -l ./check_app_status.sh'
                sh './check_app_status.sh' // Pastikan path dan permission execute sudah benar
            }
            post {
                failure {
                    script {
                        sshagent(credentials: ['server_aplikasi_ssh']) {
                            // Perbaikan: Gunakan variabel dengan ${} dan pastikan container ada
                            sh """
                                if ! docker ps --filter name=${dreamy_shockley} --filter status=running | grep ${dreamy_shockley}; then
                                    echo "Container tidak berjalan, mencoba untuk memulai ulang..."
                                    docker start ${dreamy_shockley}
                                else
                                    echo "Container sudah berjalan, mencoba untuk merestart..."
                                    docker restart ${dreamy_shockley}
                                fi
                            """

                            // Email notifikasi (opsional) - Contoh menggunakan 'emailext' plugin
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
                        // Email notifikasi (opsional) - Contoh menggunakan 'emailext' plugin
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

