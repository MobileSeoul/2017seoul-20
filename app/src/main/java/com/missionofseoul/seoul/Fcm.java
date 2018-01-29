package com.missionofseoul.seoul;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hyunho on 2017-02-17.
 */

public class Fcm {

    //토큰을 서버에 전송
    public void sendRegistrationToServer(final String token) {

        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/fcm/save_token.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);


                    String data = "id=" + G.mem_id + "&token=" + token;
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//emd of run()
        }.start();
    }//end of sendRegistrationToServer 서버에 fcm token 저장 하는 메서드

    //여행톡 글에 댓글이 달리면 글쓴 사람에게 푸쉬 메세지 보내기
    public void sendTravelTalkFcm(final int talk_no) {

        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/fcm/fcm_traveltalk.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "talk_no=" + talk_no;
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }


                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//emd of run()
        }.start();
    }//end of sendTravelTalkFcm 여행톡 글쓴 사람에게 댓글 알림


    //여행친구찾기에 댓글이 달리면 글 작성자에게 푸쉬알림 보내기
    public void sendFindFriendFcm(final int find_no) {

        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/fcm/fcm_findfriend.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "find_no=" + find_no;
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//emd of run()
        }.start();

    }//end of sendFindFriendFcm 여행친구찾기 댓글 알림 끝

}//end of class
