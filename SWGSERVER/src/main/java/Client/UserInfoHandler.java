/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *회원가입 했을때 예약에서 읽기
 * @author adsd3
 */
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class UserInfoHandler {

    private final Socket socket;
    private final BufferedWriter out;

    public UserInfoHandler(Socket socket, BufferedWriter out) {
        this.socket = socket;
        this.out = out;
    }

    public void handle(String message) throws IOException {
        if (!message.startsWith("INFO_REQUEST:")) return;

        String requestedId = message.substring("INFO_REQUEST:".length()).trim();

        File infoFile = new File("src/main/resources/USER_INFO.txt");
        if (!infoFile.exists()) {
            out.write("INFO_RESPONSE:NOT_FOUND\n");
            out.flush();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String studentId = parts[0];
                    String password = parts[1];
                    String name = parts[2];
                    String department = parts[3];
                    String role = parts[4];

                    if (studentId.equals(requestedId)) {
                        String response = String.format("INFO_RESPONSE:%s,%s,%s,%s\n", studentId, name, department, role);
                        System.out.println(response);
                        out.write(response);
                        out.flush();
                        return;
                    }
                }
            }
        }

        out.write("INFO_RESPONSE:NOT_FOUND\n");
        out.flush();
    }
    // duatmddnr,1234,염승욱,컴소,교수
    public void getUserInfo() {
        String path = "src/main/resources/USER_INFO.txt";
        StringBuilder sb = new StringBuilder("USER_INFO:");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[4].equals("학생")) {
                    if (sb.length() > "USER_INFO:".length()) {
                        sb.append(";");
                    }
                    sb.append(parts[0]); // 학번
                    sb.append(",");
                    sb.append(parts[2]); // 이름
                    sb.append(",");
                    sb.append(parts[3]); // 전공
                }
            }
            try {
                out.write(sb.toString());
                out.newLine();
                out.flush();
            } catch (IOException e) {
                System.out.println("에러발생: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("에러발생: " + e.getMessage());
        }
    }
}