/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author adsd3
 *  @author 염승욱
 * @since 2025-11-10
 * @modified 백업 폴더 생성
 * @modified 기존 파일 복사 후 백업 파일 붙여넣기
 */

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class FileSyncManager {
    private final String baseDir = "src/main/resources";
    private final String backupDir = "src/main/backups";
    private File folder;
    private final Path baseDirectory;
    private final Path backupRootDir;
    
    public FileSyncManager() {
        folder = new File(backupDir);
        if (!folder.exists()) {
            try {
                folder.mkdir();
                System.out.println("백업 폴더 생성");
            } catch (Exception e) {
                System.out.println("에러" + e);
            }
        } else {
            System.out.println("백업 폴더가 이미 존재합니다.");
        }
        backupRootDir = Paths.get(backupDir);
        baseDirectory = Paths.get(baseDir);
    }
    
    public String createBackup() {
        try {
            // 오늘 날짜 저장(포멧 방식: YYYY-MM-DD)
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            // 기존 폴더에 경로 추가
            Path targetDir = backupRootDir.resolve(today);
            // 폴더 생성
            Files.createDirectories(targetDir);
            
            try (Stream<Path> stream = Files.walk(baseDirectory)) {
                stream.filter(Files::isRegularFile)
                      .forEach(targetFile -> {
                          Path copyFile = targetDir.resolve(baseDirectory.relativize(targetFile));
                          System.out.println("상대경로: "+ copyFile);
                          // 파일 복사, 이미 존재한다면 덮어쓰기
                          try {
                            Files.copy(targetFile, copyFile, StandardCopyOption.REPLACE_EXISTING);
                          } catch (IOException e) {
                              System.out.println("파일 복사 실패" + e);
                              throw new RuntimeException("파일 복사 실패" + e);
                          }
                      });
            } catch (IOException e) {
                System.out.println("파일 복사 실패" + e);
                return "BACKUP_FAIL";
            }
            System.out.println("백업 성공" + targetDir);
            return "BACKUP_SUCCESS";
        } catch (Exception e) {
            System.out.println("백업 실패" + e);
            return "BACKUP_FAIL";
        }
    }
    
    public synchronized void updateFile(String filename, String content) throws IOException {
        File file = new File(baseDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
        System.out.println("[서버] 파일 업데이트 완료: " + filename);
    }
}