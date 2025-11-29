/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Server.*;
import Client.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CommandFactoryTest {
    @Mock Socket socket;
    @Mock BufferedWriter out;
    @Mock SessionManager sessionManager;
    @Mock LoginProcessor loginProcessor;

    CommandFactory commandFactory;

    @BeforeEach
    void setUp() {
        // Factory 생성 시 필요한 가짜 의존성 주입
        commandFactory = new CommandFactory(socket, out, sessionManager, loginProcessor);
    }

    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsBackupRequest() {
        System.out.println("BACKUP_REQUEST Command 확인");
        // When
        Command command = commandFactory.getCommand("BACKUP_REQUEST");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof BackupCommand, "BACKUP_REQUEST는 BackupCommand 클래스를 반환해야 합니다.");
        System.out.println("BACKUP_REQUEST Command 확인 성공");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsUserInfo() {
        // When
        Command command = commandFactory.getCommand("USER_INFO");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof UserInfoCommand, 
                "USER_INFO는 UserInfoOCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsRegister() {
        // When
        Command command = commandFactory.getCommand("REGISTER:");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof RegisterCommand, 
                "REGISTER:는 RegisterCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsCancelReservation() {
        // When
        Command command = commandFactory.getCommand("CANCEL_RESERVATION");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof CancelReservationCommand, 
                "CANCEL_RESERVATION는 CancelReservationCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsInfoRequest() {
        // When
        Command command = commandFactory.getCommand("INFO_REQUEST:");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof InfoRequestCommand, 
                "INFO_REQUEST:는 InfoRequestCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsFileUpdate() {
        // When
        Command command = commandFactory.getCommand("FILE_UPDATE:");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof FileUpdateCommand, 
                "FILE_UPDATE:는 FileUpdateCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsLogin() {
        // When
        Command command = commandFactory.getCommand("LOGIN:");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof LoginCommand, 
                "LOGIN:는 LoginCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnBackupCommand_WhenMessageIsFindPassword() {
        // When
        Command command = commandFactory.getCommand("FIND_PASSWORD:");

        // Then
        assertNotNull(command);
        assertTrue(command instanceof FindPasswordCommand, 
                "FIND_PASSWORD:는 FindPasswordCommand 클래스를 반환해야 합니다.");
    }
    
    @Test
    void getCommand_ShouldReturnNull_WhenMessageIsUnknown() {
        // When
        Command command = commandFactory.getCommand("UNKNOWN_COMMAND");

        // Then
        assertNull(command);
    }
}
