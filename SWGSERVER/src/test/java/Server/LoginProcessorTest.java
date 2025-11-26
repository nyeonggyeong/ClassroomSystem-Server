/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LoginProcessorTest {

    private LoginProcessor loginProcessor;
    
    @BeforeAll
    public static void setUpClass() throws IOException {
        System.out.println("===== 테스트 파일 생성 시작 =====");
        
        // 현재 작업 디렉토리 확인
        System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));
        
        // src/main/resources 디렉토리 생성
        Path resourcePath = Paths.get("src/main/resources");
        Files.createDirectories(resourcePath);
        System.out.println("리소스 디렉토리 생성: " + resourcePath.toAbsolutePath());
        
        // 기존 파일 백업
        backupFileIfExists("src/main/resources/ADMIN_LOGIN.txt");
        backupFileIfExists("src/main/resources/USER_LOGIN.txt");
        backupFileIfExists("src/main/resources/PROFESSOR_LOGIN.txt");
        
        // 테스트용 파일 생성
        createTestLoginFile("src/main/resources/ADMIN_LOGIN.txt", 
            "admin1,password123\nadmin2,pass456");
        createTestLoginFile("src/main/resources/USER_LOGIN.txt", 
            "user1,userpass1\nuser2,userpass2\nuser3,userpass3");
        createTestLoginFile("src/main/resources/PROFESSOR_LOGIN.txt", 
            "prof1,profpass1\nprof2,profpass2");
        
        // 파일 생성 확인
        verifyFileCreated("src/main/resources/ADMIN_LOGIN.txt");
        verifyFileCreated("src/main/resources/USER_LOGIN.txt");
        verifyFileCreated("src/main/resources/PROFESSOR_LOGIN.txt");
        
        System.out.println("===== 테스트 파일 생성 완료 =====\n");
    }
    
    @AfterAll
    public static void tearDownClass() throws IOException {
        System.out.println("===== 테스트 파일 정리 시작 =====");
        
        // 테스트 파일 삭제
        Files.deleteIfExists(Paths.get("src/main/resources/ADMIN_LOGIN.txt"));
        Files.deleteIfExists(Paths.get("src/main/resources/USER_LOGIN.txt"));
        Files.deleteIfExists(Paths.get("src/main/resources/PROFESSOR_LOGIN.txt"));
        
        // 백업 파일 복원
        restoreBackupIfExists("src/main/resources/ADMIN_LOGIN.txt");
        restoreBackupIfExists("src/main/resources/USER_LOGIN.txt");
        restoreBackupIfExists("src/main/resources/PROFESSOR_LOGIN.txt");
        
        System.out.println("===== 테스트 파일 정리 완료 =====");
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        loginProcessor = new LoginProcessor();
        resetSessionManager();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        resetSessionManager();
    }
    
    private static void backupFileIfExists(String path) throws IOException {
        Path source = Paths.get(path);
        if (Files.exists(source)) {
            Path backup = Paths.get(path + ".backup");
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("백업 완료: " + path + " -> " + backup);
        }
    }
    
    private static void restoreBackupIfExists(String path) throws IOException {
        Path backup = Paths.get(path + ".backup");
        if (Files.exists(backup)) {
            Files.move(backup, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("복원 완료: " + backup + " -> " + path);
        }
    }
    
    private static void createTestLoginFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        }
        System.out.println("파일 생성: " + filePath.toAbsolutePath());
    }
    
    private static void verifyFileCreated(String path) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            System.err.println("❌ 파일이 생성되지 않음: " + filePath.toAbsolutePath());
            return;
        }
        
        System.out.println("✓ 파일 존재 확인: " + filePath.toAbsolutePath());
        System.out.println("  파일 크기: " + Files.size(filePath) + " bytes");
        System.out.println("  파일 내용:");
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println("    " + lineNum + ": [" + line + "]");
                lineNum++;
            }
        }
        System.out.println();
    }
    
    private void resetSessionManager() throws Exception {
        SessionManager manager = SessionManager.getInstance();
        
        var activeField = SessionManager.class.getDeclaredField("active");
        activeField.setAccessible(true);
        ((java.util.Set<?>) activeField.get(manager)).clear();
        
        var queueField = SessionManager.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        ((java.util.Queue<?>) queueField.get(manager)).clear();
        
        var cancelUsersField = SessionManager.class.getDeclaredField("cancelUsers");
        cancelUsersField.setAccessible(true);
        ((java.util.Set<?>) cancelUsersField.get(manager)).clear();
        
        var userStreamsField = SessionManager.class.getDeclaredField("userStreams");
        userStreamsField.setAccessible(true);
        ((java.util.Map<?, ?>) userStreamsField.get(manager)).clear();
    }
    
    // ==================== validateLogin 테스트 ====================
    
    @Test
    public void testValidateLogin_AdminSuccess() {
        System.out.println("\n>>> testValidateLogin_AdminSuccess 시작");
        boolean result = loginProcessor.validateLogin("admin1", "password123", "admin");
        System.out.println("결과: " + result);
        Assertions.assertTrue(result, "관리자 로그인 성공해야 함");
    }
    
    @Test
    public void testValidateLogin_AdminFailWrongPassword() {
        System.out.println("\n>>> testValidateLogin_AdminFailWrongPassword 시작");
        boolean result = loginProcessor.validateLogin("admin1", "wrongpassword", "admin");
        System.out.println("결과: " + result);
        Assertions.assertFalse(result, "잘못된 비밀번호로 로그인 실패해야 함");
    }
    
    @Test
    public void testValidateLogin_AdminFailWrongId() {
        System.out.println("\n>>> testValidateLogin_AdminFailWrongId 시작");
        boolean result = loginProcessor.validateLogin("wrongadmin", "password123", "admin");
        System.out.println("결과: " + result);
        Assertions.assertFalse(result, "존재하지 않는 아이디로 로그인 실패해야 함");
    }
    
    @Test
    public void testValidateLogin_UserSuccess() {
        System.out.println("\n>>> testValidateLogin_UserSuccess 시작");
        boolean result = loginProcessor.validateLogin("user1", "userpass1", "user");
        System.out.println("결과: " + result);
        Assertions.assertTrue(result, "사용자 로그인 성공해야 함");
    }
    
    @Test
    public void testValidateLogin_UserFailWrongPassword() {
        System.out.println("\n>>> testValidateLogin_UserFailWrongPassword 시작");
        boolean result = loginProcessor.validateLogin("user1", "wrongpass", "user");
        System.out.println("결과: " + result);
        Assertions.assertFalse(result, "잘못된 비밀번호로 로그인 실패해야 함");
    }
    
    @Test
    public void testValidateLogin_ProfessorSuccess() {
        System.out.println("\n>>> testValidateLogin_ProfessorSuccess 시작");
        boolean result = loginProcessor.validateLogin("prof1", "profpass1", "professor");
        System.out.println("결과: " + result);
        Assertions.assertTrue(result, "교수 로그인 성공해야 함");
    }
    
    @Test
    public void testValidateLogin_ProfessorFailWrongPassword() {
        System.out.println("\n>>> testValidateLogin_ProfessorFailWrongPassword 시작");
        boolean result = loginProcessor.validateLogin("prof1", "wrongpass", "professor");
        System.out.println("결과: " + result);
        Assertions.assertFalse(result, "잘못된 비밀번호로 로그인 실패해야 함");
    }
    
    @Test
    public void testValidateLogin_WithWhitespace() {
        System.out.println("\n>>> testValidateLogin_WithWhitespace 시작");
        boolean result = loginProcessor.validateLogin("user1 ", " userpass1", "user");
        System.out.println("결과: " + result);
        Assertions.assertFalse(result, "공백이 포함된 경우 실패해야 함");
    }
    
    // ==================== tryUserLogin 테스트 ====================
    
    @Test
    public void testTryUserLogin_Success() {
        System.out.println("\n>>> testTryUserLogin_Success 시작");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out))) {
            SessionManager.LoginDecision result = loginProcessor.tryUserLogin("testUser1", out);
            System.out.println("결과: " + result);
            Assertions.assertEquals(SessionManager.LoginDecision.OK, result, 
                "첫 번째 사용자 로그인 성공해야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
    
    @Test
    public void testTryUserLogin_DuplicateLogin() {
        System.out.println("\n>>> testTryUserLogin_DuplicateLogin 시작");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out))) {
            loginProcessor.tryUserLogin("testUser1", out);
            SessionManager.LoginDecision result = loginProcessor.tryUserLogin("testUser1", out);
            System.out.println("결과: " + result);
            Assertions.assertEquals(SessionManager.LoginDecision.FAIL_DUP, result, 
                "중복 로그인 시도 시 FAIL_DUP 반환해야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
    
    @Test
    public void testTryUserLogin_WaitingQueue() {
        System.out.println("\n>>> testTryUserLogin_WaitingQueue 시작");
        try (BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(System.out))) {
            
            loginProcessor.tryUserLogin("user1", out1);
            loginProcessor.tryUserLogin("user2", out2);
            loginProcessor.tryUserLogin("user3", out3);
            loginProcessor.tryUserLogin("user4", out4);
            
            SessionManager.LoginDecision result = loginProcessor.tryUserLogin("user5", out5);
            System.out.println("결과: " + result);
            Assertions.assertEquals(SessionManager.LoginDecision.WAIT, result, 
                "정원 초과 시 WAIT 반환해야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
    
    @Test
    public void testLogout_Success() {
        System.out.println("\n>>> testLogout_Success 시작");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out))) {
            loginProcessor.tryUserLogin("testUser1", out);
            loginProcessor.logout("testUser1");
            
            SessionManager.LoginDecision result = loginProcessor.tryUserLogin("testUser1", out);
            System.out.println("결과: " + result);
            Assertions.assertEquals(SessionManager.LoginDecision.OK, result, 
                "로그아웃 후 다시 로그인 가능해야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
    
    @Test
    public void testLogout_QueueProcessing() {
        System.out.println("\n>>> testLogout_QueueProcessing 시작");
        try (BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(System.out));
             BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(System.out))) {
            
            loginProcessor.tryUserLogin("user1", out1);
            loginProcessor.tryUserLogin("user2", out2);
            loginProcessor.tryUserLogin("user3", out3);
            loginProcessor.tryUserLogin("user4", out4);
            
            SessionManager.LoginDecision result5 = loginProcessor.tryUserLogin("user5", out5);
            Assertions.assertEquals(SessionManager.LoginDecision.WAIT, result5);
            
            loginProcessor.logout("user1");
            
            SessionManager.LoginDecision result5Again = loginProcessor.tryUserLogin("user5", out5);
            System.out.println("결과: " + result5Again);
            Assertions.assertEquals(SessionManager.LoginDecision.FAIL_DUP, result5Again, 
                "대기자 자동 로그인 후 중복 로그인 방지되어야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
    
    @Test
    public void testIntegration_ValidateAndLogin() {
        System.out.println("\n>>> testIntegration_ValidateAndLogin 시작");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out))) {
            boolean isValid = loginProcessor.validateLogin("user1", "userpass1", "user");
            System.out.println("validateLogin 결과: " + isValid);
            Assertions.assertTrue(isValid, "유효한 계정이어야 함");
            
            SessionManager.LoginDecision loginResult = loginProcessor.tryUserLogin("user1", out);
            System.out.println("tryUserLogin 결과: " + loginResult);
            Assertions.assertEquals(SessionManager.LoginDecision.OK, loginResult, 
                "로그인 성공해야 함");
            
            loginProcessor.logout("user1");
            
            SessionManager.LoginDecision reLoginResult = loginProcessor.tryUserLogin("user1", out);
            System.out.println("재로그인 결과: " + reLoginResult);
            Assertions.assertEquals(SessionManager.LoginDecision.OK, reLoginResult, 
                "재로그인 가능해야 함");
        } catch (IOException e) {
            Assertions.fail("IOException 발생: " + e.getMessage());
        }
    }
}