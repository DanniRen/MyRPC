package org.example.rpcVersion3.NIO;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelTest {
    public static void main(String[] args) throws IOException {
//        write("./nio.txt", "Hello NIO!");
//        read("./nio.txt");
        copyFile("./nio.txt", "./nio_copy.txt");
    }

    public static void write(String path, String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);

        FileChannel fileChannel = fos.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put(content.getBytes());

        buffer.flip();

        fileChannel.write(buffer);

        fos.close();


    }

    public static void read(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(path);
        FileChannel fileChannel = fis.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        fileChannel.read(buffer);

        String content = new String(buffer.array());
        System.out.println(content);
    }

    public static void copyFile(String srcPath, String destPath) throws IOException {
        File file = new File(srcPath);
        FileInputStream fis = new FileInputStream(file);

        FileChannel fileChannel = fis.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        fileChannel.read(buffer);

        buffer.flip();

        FileOutputStream fos = new FileOutputStream(destPath);
        FileChannel fileChannel2 = fos.getChannel();
        fileChannel2.write(buffer);
        fos.close();
        fis.close();
    }
}
