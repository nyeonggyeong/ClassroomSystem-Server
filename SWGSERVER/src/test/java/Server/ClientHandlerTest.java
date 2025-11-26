/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClientHandlerTest {

    @Mock
    Socket socket;

    @Mock
    SessionManager sessionManager;

    @Mock
    LoginProcessor loginProcessor;

    @Mock
    CommandFactory commandFactory;

    @Mock
    Command mockCommand;

    private PipedOutputStream clientOutput;
    private PipedInputStream serverInput;
    private PipedOutputStream serverOutput;
    private PipedInputStream clientInput;

    @BeforeEach
    void setUp() throws IOException {
        // 파이프 스트림 설정 (클라이언트 ↔ 서버 통신 시뮬레이션)
        clientOutput = new PipedOutputStream();
        serverInput = new PipedInputStream(clientOutput);

        serverOutput = new PipedOutputStream();
        clientInput = new PipedInputStream(serverOutput);

        // Socket의 스트림 Mock 설정
        lenient().when(socket.getInputStream()).thenReturn(serverInput);
        lenient().when(socket.getOutputStream()).thenReturn(serverOutput);
    }

    // [Helper 메서드] Private 필드 값 설정하기 (리플렉션)
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // [Helper 메서드] Private 필드 값 가져오기 (리플렉션)
    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    @DisplayName("BACKUP_REQUEST 메시지 처리 - Command 실행 확인")
    void testClientHandler_BackupRequest_ExecutesCommand() throws Exception {
        try (MockedStatic<SessionManager> mockedStatic = mockStatic(SessionManager.class)) {
            mockedStatic.when(SessionManager::getInstance).thenReturn(sessionManager);

            // 1. 실제 핸들러 생성 (생성자 내부 로직 실행)
            ClientHandler handler = new ClientHandler(socket);

            // 2. [핵심] 리플렉션으로 Mock CommandFactory 강제 주입
            setPrivateField(handler, "commandFactory", commandFactory);

            // Mock 설정
            when(commandFactory.getCommand("BACKUP_REQUEST")).thenReturn(mockCommand);

            // 클라이언트 메시지 전송
            PrintWriter clientWriter = new PrintWriter(clientOutput, true);
            clientWriter.println("BACKUP_REQUEST");
            clientWriter.println("LOGOUT");

            // When: 스레드 실행
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            handlerThread.join(2000);

            // Then
            verify(commandFactory, times(1)).getCommand("BACKUP_REQUEST");
            verify(mockCommand, times(1)).execute(eq("BACKUP_REQUEST"), any(BufferedReader.class));
        }
    }

    @Test
    @DisplayName("LOGIN 메시지 처리 - userId 업데이트 확인")
    void testClientHandler_LoginRequest_UpdatesUserId() throws Exception {
        try (MockedStatic<SessionManager> mockedStatic = mockStatic(SessionManager.class)) {
            mockedStatic.when(SessionManager::getInstance).thenReturn(sessionManager);

            ClientHandler handler = new ClientHandler(socket);

            // Mock Factory 주입
            setPrivateField(handler, "commandFactory", commandFactory);

            when(commandFactory.getCommand(startsWith("LOGIN:"))).thenReturn(mockCommand);
            when(commandFactory.getUserId()).thenReturn("testUser123");

            PrintWriter clientWriter = new PrintWriter(clientOutput, true);
            clientWriter.println("LOGIN:testUser123,password,user");
            clientWriter.println("LOGOUT");

            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            handlerThread.join(2000);

            // Then: userId 필드가 업데이트 되었는지 리플렉션으로 확인
            String updatedUserId = (String) getPrivateField(handler, "userId");
            assertEquals("testUser123", updatedUserId);
        }
    }

    @Test
    @DisplayName("LOGOUT 메시지 처리 - 세션 로그아웃 및 스레드 종료")
    void testClientHandler_Logout_ClosesConnection() throws Exception {
        try (MockedStatic<SessionManager> mockedStatic = mockStatic(SessionManager.class)) {
            mockedStatic.when(SessionManager::getInstance).thenReturn(sessionManager);
            
            ClientHandler handler = new ClientHandler(socket);
            setPrivateField(handler, "commandFactory", commandFactory);
            
            // 로그인 상태 시뮬레이션을 위해 리플렉션으로 userId 미리 설정
            setPrivateField(handler, "userId", "testUser");
            
            PrintWriter clientWriter = new PrintWriter(clientOutput, true);
            clientWriter.println("LOGOUT");
            
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            handlerThread.join(2000);
            
            // [수정됨] 실제 로직상 명시적 로그아웃 + finally 블록 로그아웃으로 총 2회 호출됨
            verify(sessionManager, times(2)).logout("testUser"); 
            
            verify(socket, times(1)).close();
        }
    }

    @Test
    @DisplayName("IOException 발생 시 정리 작업 수행")
    void testClientHandler_IOException_PerformsCleanup() throws Exception {
        try (MockedStatic<SessionManager> mockedStatic = mockStatic(SessionManager.class)) {
            mockedStatic.when(SessionManager::getInstance).thenReturn(sessionManager);

            Socket faultySocket = mock(Socket.class);
            InputStream faultyInput = mock(InputStream.class);

            lenient().when(faultySocket.getInputStream()).thenReturn(faultyInput);
            lenient().when(faultySocket.getOutputStream()).thenReturn(serverOutput);

            // 읽기 시도 시 예외 발생
            when(faultyInput.read(any(), anyInt(), anyInt())).thenThrow(new IOException("Connection lost"));

            ClientHandler handler = new ClientHandler(faultySocket);
            setPrivateField(handler, "commandFactory", commandFactory);
            setPrivateField(handler, "userId", "testUser"); // userId 강제 설정

            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            handlerThread.join(2000);

            verify(sessionManager, times(1)).logout("testUser");
            verify(faultySocket, times(1)).close();
        }
    }
}
