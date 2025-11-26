/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Server.BackupCommand;
import Client.FileSyncManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackupCommandTest {
    @Mock
    FileSyncManager fileSyncManager; // 가짜(Mock) 객체 생성

    @Mock
    BufferedReader bufferedReader; // execute 메서드 인자용

    @Test
    void execute_ShouldCallCreateBackup() throws IOException {
        // Given: 커맨드 객체 생성 (Mock Manager 주입)
        BackupCommand command = new BackupCommand(fileSyncManager);

        // When: 실행
        command.execute("BACKUP_REQUEST", bufferedReader);

        // Then: Manager의 createBackup()이 딱 1번 호출되었는지 검증
        verify(fileSyncManager, times(1)).createBackup();
    }
}
