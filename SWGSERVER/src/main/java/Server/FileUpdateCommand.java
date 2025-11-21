/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.FileSyncManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author user
 */
public class FileUpdateCommand implements Command {
    private FileSyncManager fileSyncManager;
    
    public FileUpdateCommand(FileSyncManager fileSyncManager) {
        this.fileSyncManager = fileSyncManager;
    }
    
    @Override
    public void execute(String message, BufferedReader in) throws IOException {
        String filename = message.substring("FILE_UPDATE:".length()).trim();
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = in.readLine()).equals("<<EOF>>")) {
            content.append(line).append("\n");
        }

        try {
            fileSyncManager.updateFile(filename, content.toString());
            
        } catch (IOException e) {
            System.err.println("[서버] 파일 동기화 실패: " + filename);
            e.printStackTrace();
        }
    }
    
}
