package com.dfbz.chat01;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Spring-Xa
 */
public class ServerChat extends JFrame {
    private static final int PORT = 9999;
    private final JTextArea serverTa = new JTextArea();
    private JScrollPane sp = new JScrollPane(serverTa);
    private final JPanel btnTool = new JPanel();
    private final JButton startBtn = new JButton("启动");
    private final JButton stopBtn = new JButton("停止");

    private ServerSocket ss = null;
    private Socket s= null;
    private ArrayList<ClientConn> ccList = new ArrayList<ClientConn>();

    private boolean isStart = false;

    private DataInputStream dis = null;

    public ServerChat() {
        this.setTitle("服务器端");
        this.add(sp, BorderLayout.CENTER);
        btnTool.add(startBtn);
        btnTool.add(stopBtn);
        this.add(btnTool,BorderLayout.SOUTH);

        this.setBounds(0,0,500,500);

        if(isStart) {
            serverTa.append("服务器已启动\n");
        }
        else {
            serverTa.append("服务器未启动，请启动\n");
        }
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isStart = false;
                try {
                    if (ss != null) {
                        ss.close();
                        isStart = false;
                    }
                    System.exit(0);
                    serverTa.append("服务器断开！");
                    System.out.println("服务器断开！");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    }
            }
        });

        startBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e1) {
                System.out.println("服务器已启动");
                try {
                    if (ss == null) {
                        ss = new ServerSocket(PORT);
                    }
                    isStart = true;
                    serverTa.append("服务器已启动\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //serverTa.setEditable(false);
        this.setVisible(true);
        startServer();


    }

    /**
     * 服务器启动
     */
    public void startServer() {
        try {
            try {
                ss = new ServerSocket(PORT);
                isStart = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            //接受多个客户端连接
            while (isStart) {
                Socket s = ss.accept();
                ccList.add(new ClientConn(s));
                System.out.println("一个客户端连接服务器" + s.getInetAddress() + "/" + s.getPort() + "\n");
                serverTa.append("一个客户端连接服务器" + s.getInetAddress() + "/" + s.getPort() + "\n");
            }
        } catch (SocketException e) {
            System.out.println("服务器中断");
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务器端的连接对象
     */
    class ClientConn implements Runnable{
        Socket s = null;
        public ClientConn(Socket s) {
            this.s = s;
            (new Thread(this)).start();
        }
        //同时接受客户端信息
        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(s.getInputStream());
                //服务器能够接受多条信息
                while(isStart) {
                    String str = dis.readUTF();
                    System.out.println(s.getInetAddress() + "|" +s.getPort() + ":" + str + "\n");
                    serverTa.append(s.getInetAddress() + "|" +s.getPort() + ":" + str + "\n");
                    String strSend = s.getInetAddress() + "|" +s.getPort() + ":" + str + "\n";
                    //遍历ccList,调用send方法,在客户端接收消息是多线程的接
                    Iterator<ClientConn> it = ccList.iterator();
                    while (it.hasNext()) {
                        ClientConn o = it.next();
                        o.send(strSend);
                    }
                }
            } catch (SocketException e) {
                System.out.println("一个客户端下线了");
                serverTa.append(s.getInetAddress() + "|" +s.getPort() + ":" + "客户端下线\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //每个连接对象发送数据的方法
        public void send(String str) {
            try {
                DataOutputStream dos = new DataOutputStream(this.s.getOutputStream());
                dos.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ServerChat();
    }
}
