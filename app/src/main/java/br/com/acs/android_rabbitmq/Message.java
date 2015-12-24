package br.com.acs.android_rabbitmq;

/**
 * Created by andersonacs on 23/12/15.
 */
public class Message {

    private String text;

    public Message(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
