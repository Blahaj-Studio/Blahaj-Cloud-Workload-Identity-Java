package net.blahajcloud.workloadidentity;

import org.newsclub.net.unix.AFVSOCKSocketAddress;
import org.newsclub.net.unix.vsock.AFVSOCKServerSocket;
import org.newsclub.net.unix.vsock.AFVSOCKSocket;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class WorkloadIdentity {
    public static String getBasicToken () {
        try {
            URL url = new URL("https://workload-identity.blahajcloud.net/attestation");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String token = reader.readLine();
            reader.close();
            connection.disconnect();
            return token;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAdvancedToken () {
        try {
            final String[] token = new String[1];

            Thread thread = new Thread(() -> {
                try {
                    AFVSOCKSocketAddress address = AFVSOCKSocketAddress.ofLocalPort(9999);

                    AFVSOCKServerSocket server = AFVSOCKServerSocket.bindOn(address);
                    AFVSOCKSocket socket = server.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    token[0] = reader.readLine();
                    reader.close();
                    socket.close();
                    server.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();

            URL url = new URL("https://workload-identity.blahajcloud.net/attestation/advanced");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) return null;
            connection.disconnect();

            while (thread.isAlive()) {}

            return token[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
