package com.dfbz.chat01;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 创建客户端
 * @author yuche
 */
public class ClientChat extends JFrame{
    private final JTextArea ta = new JTextArea(10,20);
    private final JTextField tf = new JTextField(20);
    private JScrollPane sp = new JScrollPane(ta);
    private static final String CONNSTR = "10.64.176.81";
    private static final int CONNPORT = 9999;
    private Socket s = null;
    private DataOutputStream dos = null;
    private boolean isConn = false;

    public ClientChat() throws HeadlessException {
        super();
    }

    public void init() {
        this.setTitle("客户端窗口");
        this.add(sp, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        this.setBounds(300,300,300,400);

        //监听事件
        tf.addActionListener(e -> {
            String strSend = tf.getText();
            if (strSend.trim().length() == 0) {
                return;
            }
            send(strSend);

            tf.setText("");
            //ta.append(strSend + "\n");
        });
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ta.setEditable(false);
        tf.requestFocus();

        try {
            s = new Socket(CONNSTR,CONNPORT);
            //表示连接上服务器
            isConn = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setVisible(true);

        //启动多线程
        new Thread(new Receive()).start();
    }

    /**
     * 发送消息
     */
    public void send(String str) {
        try {
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多线程的类,实现Runnable接口
     */
    class Receive implements Runnable {

        @Override
        public void run() {
            try {
                while(isConn) {
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    String str = dis.readUTF();
                    ta.append(str);
                }
            } catch (SocketException e) {
                System.out.println("服务器中断");
                ta.append("服务器中断\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        ClientChat cc = new ClientChat();
        cc.init();
    }
}
