package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.model.DisplayMessageFormat;
import org.uniupo.it.model.Selection;
import org.uniupo.it.model.State;
import org.uniupo.it.util.Topics;

public class TransactionService {
    final private String machineId;
    final private MqttClient mqttClient;
    final private String baseTopic;
    final private State state;
    final private Gson gson;
    private String selectedBeverage;

    public TransactionService(String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.gson = new Gson();
        this.baseTopic = "macchina/" + machineId + "/transaction";
        this.mqttClient.subscribe(baseTopic + "/newSelection", this::newSelectionHandler);
        this.state = State.IDLE;
        this.selectedBeverage = "beverageCode";
    }

    private void newSelectionHandler(String topic, MqttMessage mqttMessage) {
        try {
            if (state != State.IDLE) {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC, new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Transaction in progress...")).getBytes()));
                return;
            }

            String jsonMessage = new String(mqttMessage.getPayload());
            this.selectedBeverage = gson.fromJson(jsonMessage, Selection.class).getDrinkCode();

            

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


        private void checkConsumableAvailability (String selectedBeverage) throws MqttException {
            String dbTopic = "macchina/" + machineId + "/db";
            mqttClient.publish(dbTopic + "/consumableAvailability", new MqttMessage(selectedBeverage.getBytes()));
        }

    }
