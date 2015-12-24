package br.com.acs.android_rabbitmq;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by andersonacs on 23/12/15.
 */
public class ManagerRabbitMQ {

    protected Channel mChannel = null;
    protected Connection mConnection;
    private static final String EXCHANGE_NAME = "exchange";


    String userName = "";
    String password = "";
    String virtualHost = "/";
    String serverIp = "";
    int port = 5672;


    protected boolean running;

    private Context context;

    public ManagerRabbitMQ(Context context) {
        this.context = context;
    }

    public void dispose(){

        running = false;

        try {
            if (mConnection!=null)
                mConnection.close();
            if (mChannel != null)
                mChannel.abort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connectToRabbitMQ() {

        if (mChannel != null && mChannel.isOpen()){//already declared
            running = true;
        }

        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {

                try{

                    final ConnectionFactory connectionFactory = new ConnectionFactory();
                    connectionFactory.setUsername(userName);
                    connectionFactory.setPassword(password);
                    connectionFactory.setVirtualHost(virtualHost);
                    connectionFactory.setHost(serverIp);
                    connectionFactory.setPort(port);
                    connectionFactory.setAutomaticRecoveryEnabled(true);

                    mConnection = connectionFactory.newConnection();
                    mChannel = mConnection.createChannel();

                    registerChanelHost();
                    registerChanelListHost();

                    return true;

                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                running = aBoolean;
            }

        }.execute();
    }

    private void registerChanelListHost(){

        try{

            mChannel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

            String queueName = mChannel.queueDeclare().getQueue();
            mChannel.queueBind(queueName, EXCHANGE_NAME, "queue.messages");

            Consumer consumer = new DefaultConsumer(mChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {

                    //Verify if device that send the info is different of the are receiver
                    getHeader(properties);

                    String message = new String(body, "UTF-8");

                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Message>>() {}.getType();

                    List<Message> messageList = gson.fromJson(message, type);

                }
            };

            mChannel.basicConsume(queueName, true, consumer);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void registerChanelHost(){

        try{

            mChannel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

            String queueName = mChannel.queueDeclare().getQueue();
            mChannel.queueBind(queueName, EXCHANGE_NAME, "queue.message");

            Consumer consumer = new DefaultConsumer(mChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {

                    //Verify if device that send the info is different of the are receiver
                    getHeader(properties);

                    String message = new String(body, "UTF-8");

                    Gson gson = new Gson();
                    Message message1 = gson.fromJson(message, Message.class);

                }
            };

            mChannel.basicConsume(queueName, true, consumer);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private  void getHeader(AMQP.BasicProperties properties){

        Map<String, Object> headers = properties.getHeaders();

        Object deviceId =  headers.get("extraContent");

    }
}
