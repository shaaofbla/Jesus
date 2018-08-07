package jesus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        InetAddress localhost = InetAddress.getByName("192.168.178.33");
        int port = 7777;
        OSCPortOut sender = new OSCPortOut(localhost, port);

        String x;
        String y;
        String data;

        while (true) {
            try {
                JSONObject json = readJsonFromUrl("http://soetscho.ga.94-231-94-128.preview5.servertown.ch/data/json.txt");
                System.out.println(json.get("x"));

                x = json.getString("x");
                y = json.getString("y");

                data = x + "-" + y;

                OSCMessage msg = new OSCMessage("/coords");
                msg.addArgument(data);
                try {
                    sender.send(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e){
                e.printStackTrace();
                System.out.println("Fuck ");
            }



            }
        }
    }

class ReadJsonInThread implements Runnable {
    String jsonUrl;
    InetAddress oscUrl;
    int oscPort;
    OSCPortOut sender;
    private Thread t;
    private String threadName;

    public ReadJsonInThread(String jsonUrl, InetAddress oscUrl, int oscPort, String threadName) {
        this.jsonUrl = jsonUrl;
        this.oscUrl = oscUrl;
        this.oscPort = oscPort;
        this.threadName = threadName;
        try {
            this.sender = new OSCPortOut(this.oscUrl, this.oscPort);
        } catch (SocketException e){
            e.printStackTrace();
        }
    }

    public void run() {
        Double x;
        Double y;
        Double lastx = 0.0;
        long TimePoint;
        long LastDiffTimePoint = 0;
        String data;
        int waitingTime = 20;
        while (true) {
            try {
                JSONObject json = JsonReader.readJsonFromUrl(jsonUrl);
                y = json.getDouble("y");
                x = json.getDouble("x");
                Thread.sleep(waitingTime);
                OSCMessage msg = new OSCMessage("/coords");
                msg.addArgument(x.toString());
                msg.addArgument(y.toString());
                waitingTime = 20;
                try {
                    sender.send(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e){
                //e.printStackTrace();
                waitingTime = 1;
                System.out.println("Fuck ");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }

    public void start() {
        System.out.println("Strating " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.setDaemon(true);
            t.start();
        }
    }
}

class TestReadJsonInThread {
    public static void main(String args[]){
        String[] json = new String[3];
        ReadJsonInThread[] readThread = new ReadJsonInThread[10];

        for (int i = 0; i<json.length; i++){
            json[i] = "http://soetscho.ga.94-231-94-128.preview5.servertown.ch/data/json_" + (i+1) + ".txt";
        }
        System.out.println(json[1]);
        try {
            for (int i = 0; i<json.length; i++) {
                InetAddress localhost = InetAddress.getByName("192.168.0.120");
                readThread[i] = new ReadJsonInThread(json[i], localhost, 7777+i, "Thread"+i);
                readThread[i].start();
            }
            while (true) {
                Thread.sleep(1000);
                System.currentTimeMillis();
            }

        } catch (UnknownHostException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
