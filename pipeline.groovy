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
                            sh "docker ps | grep ${dreamy_shockley} && docker restart ${dreamy_shockley}"
                            // Atau, gunakan ID container jika lebih reliable:
                            // sh "docker restart $(docker ps -q --filter name=${CONTAINER_NAME})"

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
                            body: """
                                Build Jenkins Anda berhasil.
                                Stage: Periksa Status Aplikasi
                                """,
                            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                        )
                    }
                }
            }
        }
    }
}
