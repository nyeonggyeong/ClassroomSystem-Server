/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Client.FileSyncManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUpdateCommandTest {

    @Mock
    FileSyncManager fileSyncManager;

    @Mock
    BufferedReader bufferedReader;

    @Test
    @DisplayName("파일 업데이트 요청 - 내용을 읽고 Manager 호출 성공")
    void execute_ShouldReadContentAndCallUpdateFile() throws IOException {
        // Given
        FileUpdateCommand command = new FileUpdateCommand(fileSyncManager);
        String message = "FILE_UPDATE:test.txt";

        // BufferedReader 동작 시뮬레이션 (순서대로 줄을 반환하다가 <<EOF>> 반환)
        when(bufferedReader.readLine())
            .thenReturn("Hello World")
            .thenReturn("This is a test file.")
            .thenReturn("<<EOF>>");

        // When
        command.execute(message, bufferedReader);

        // Then
        // 1. 파일 이름이 "test.txt"로 잘 파싱되었는지
        // 2. 내용이 개행문자(\n)와 함께 잘 합쳐졌는지 확인
        String expectedContent = "Hello World\nThis is a test file.\n";
        verify(fileSyncManager, times(1)).updateFile("test.txt", expectedContent);
    }

    @Test
    @DisplayName("내용 없는 파일 업데이트 - 즉시 EOF 수신")
    void execute_ShouldHandleEmptyContent() throws IOException {
        // Given
        FileUpdateCommand command = new FileUpdateCommand(fileSyncManager);
        String message = "FILE_UPDATE:empty.txt";

        // 바로 종료 신호 수신
        when(bufferedReader.readLine()).thenReturn("<<EOF>>");

        // When
        command.execute(message, bufferedReader);

        // Then: 빈 문자열("")로 업데이트 호출됨
        verify(fileSyncManager, times(1)).updateFile("empty.txt", "");
    }

    @Test
    @DisplayName("업데이트 중 IOException 발생 - 예외를 잡고 로그 출력 (중단되지 않음)")
    void execute_ShouldCatchIOException_FromManager() throws IOException {
        // Given
        FileUpdateCommand command = new FileUpdateCommand(fileSyncManager);
        String message = "FILE_UPDATE:error.txt";

        when(bufferedReader.readLine()).thenReturn("content").thenReturn("<<EOF>>");

        // Manager가 updateFile 호출 시 IOException을 던지도록 설정
        doThrow(new IOException("Disk Full")).when(fileSyncManager).updateFile(anyString(), anyString());

        // When & Then
        // 예외가 catch 블록에서 처리되므로, execute 메서드 밖으로 던져지지 않아야 함 (테스트 성공)
        command.execute(message, bufferedReader);

        // 호출은 시도했는지 검증
        verify(fileSyncManager, times(1)).updateFile(eq("error.txt"), anyString());
    }
}
