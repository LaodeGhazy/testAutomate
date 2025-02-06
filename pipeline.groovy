pipeline {
    agent any
    environment {
        IMAGE_NAME = "my-web-app" // Ganti dengan nama image Docker Anda yang sesuai
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'server_aplikasi_ssh', url: 'https://github.com/LaodeGhazy/testAutomate.git']]])
            }
        }
        stage('Periksa Status Aplikasi') {
            steps {
                // Menampilkan file check_app_status.sh untuk memastikan path dan permission benar
                sh 'ls -l ./check_app_status.sh'
                
                // Menjalankan script untuk memeriksa status aplikasi
                sh './check_app_status.sh' // Pastikan file ini ada dan bisa dijalankan
            }
            post {
                failure {
                    script {
                        sshagent(credentials: ['server_aplikasi_ssh']) {
                            // Debugging dengan set -x untuk melacak perintah yang dijalankan
                            sh """
                                set -x
                                
                                // Mengecek apakah container berdasarkan image sudah berjalan
                                if ! docker ps -a --filter ancestor=${IMAGE_NAME} --filter status=running | grep ${IMAGE_NAME}; then
                                    echo "Container tidak berjalan, mencoba untuk memulai ulang..."
                                    // Menghapus container lama yang sudah mati jika ada
                                    docker ps -q --filter ancestor=${IMAGE_NAME} --filter status=exited | xargs docker rm
                                    // Menjalankan container baru
                                    docker run -d -p 5000:5000 ${IMAGE_NAME}
                                else
                                    echo "Container sudah berjalan, mencoba untuk merestart..."
                                    // Menghentikan dan menghapus container yang sedang berjalan
                                    docker ps -q --filter ancestor=${IMAGE_NAME} --filter status=running | xargs docker stop
                                    docker ps -q --filter ancestor=${IMAGE_NAME} --filter status=running | xargs docker rm
                                    // Menjalankan ulang container
                                    docker run -d -p 5000:5000 ${IMAGE_NAME}
                                fi
                                
                                // Menampilkan log untuk debugging lebih lanjut
                                docker logs \$(docker ps -q --filter ancestor=${IMAGE_NAME} --filter status=running)
                            """
                            
                            // Mengirim notifikasi email jika build gagal
                            emailext (
                                subject: "Jenkins Build Gagal",
                                body: """
                                    Build Jenkins Anda gagal.
                                    Stage: Periksa Status Aplikasi
                                    Status Code: ${env.STATUS_CODE}
                                    Detail: Build gagal pada tahap pengecekan aplikasi.
                                """,
                                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                            )
                        }
                    }
                }
                success {
                    script {
                        // Mengirim notifikasi email jika build berhasil
                        emailext (
                            subject: "Jenkins Build Berhasil",
                            body: "Build Jenkins Anda berhasil dan aplikasi berjalan normal.",
                            to: "ghazylaode002@gmail.com"
                        )
                    }
                }
            }
        }
    }
}

