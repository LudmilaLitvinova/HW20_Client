package ua.hillellit.model;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class TestRunnableClientTester implements Runnable {
    static Socket socket;

    public TestRunnableClientTester() {
        try {

            // создаём сокет общения на стороне клиента в конструкторе объекта
            socket = new Socket("localhost", 3345);
            System.out.println("Client connected to socket");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try (Socket socket = new Socket("localhost", 3345);
             DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
             DataInputStream ois = new DataInputStream(socket.getInputStream())) {

            while (!socket.isInputShutdown()) {
                if (ois.available() != 0) {
                    System.out.println("reading...");
                    String in = ois.readUTF();
                    System.out.println(in);
                }

                Thread.sleep(3000);


                for (int i = 1; i <= 10; i++) {
                    int ver = (int) (Math.random() * 10);
                    String clientCommand;

                    switch (ver) {
                        case 8:
                        case 9:
                            clientCommand = "file";
                            break;
                        case 10:
                            clientCommand = "exit";
                            break;
                        default:
                            clientCommand = "Hello #" + i;
                    }

                    oos.writeUTF(clientCommand);
                    oos.flush();
                    System.out.println("Client sent message " + clientCommand + " to server.");

                    if (clientCommand.equalsIgnoreCase("file")) {
                        sentFile("src\\main\\resources\\data.txt");
                    }
                    if (clientCommand.equalsIgnoreCase("exit")) {
                        System.out.println("Client kill connections");
                        Thread.sleep(1000);
                        break;
                    }

                    Thread.sleep(3000);
                }
                oos.writeUTF("exit");
                oos.flush();
                break;
            }

            System.out.println("Closing connections & channels on clientSide - DONE.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sentFile(String file_name) throws IOException {
        File file = new File(file_name);
        long length = file.length();

        byte[] bytes = new byte[4000 * 1024];
        InputStream in1 = Files.newInputStream(file.toPath());
        OutputStream out1 = socket.getOutputStream();

        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(length);
//        System.out.println(length);

        out1.write(buf.array());
//        System.out.println(Arrays.toString(buf.array()));

        int count;
        while ((count = in1.read(bytes)) != -1) {
            out1.write(bytes, 0, count);
//            System.out.println(count);


        }
        out1.close();
        in1.close();
        System.out.println("file send");
    }
}
