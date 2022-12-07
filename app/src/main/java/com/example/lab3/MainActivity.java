package com.example.lab3;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer soundFeedback;
    private TextView txv_rgb;
    private Button btn_color;
    int RGBMessage;
    int r;
    int g;
    int b;
    String[] colorValues;

    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "MainActivity";

    private static final String TOPICRGB = "RGB"; // YOUR TOPIC HERE, must match the Python script!!!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txv_rgb = (TextView) findViewById(R.id.txv_rgbValue);
        btn_color = (Button) findViewById(R.id.btnColor);
        soundFeedback = MediaPlayer.create(this, R.raw.blue);


        connect();

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe(TOPICRGB);
                } else {
                    System.out.println("Connected to: " + serverURI);
                    subscribe(TOPICRGB);
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals("RGB")){
                    colorValues = new String(message.getPayload()).split(",");
                    r = Integer.parseInt(colorValues[0]);
                    g = Integer.parseInt(colorValues[1]);
                    b = Integer.parseInt(colorValues[2]);
                    RGBMessage = Color.rgb(r, g, b);
                }
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);

                /* add code here to interact with elements
                 (text views, buttons)
                 using data from newMessage
                */
                // Uncomment accordingly
                txv_rgb.setText(r + ", " + g + ", " + b);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        btn_color.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Add code to execute on click
                btn_color.setTextColor(RGBMessage);
                txv_rgb.setTextColor(RGBMessage);
                soundFeedback.start();
            }
        });
    }

    private void connect(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}





