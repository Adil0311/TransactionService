package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.model.DisplayMessageFormat;
import org.uniupo.it.model.DrinkAvailabilityResult;
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
        this.mqttClient.subscribe(baseTopic + "/consumableAvailabilityResponse", this::consumableAvailabilityResponseHandler);
        this.state = State.IDLE;
        this.selectedBeverage = "beverageCode";

        Selection s = new Selection("4", 1);
        String dispenserTopic = String.format(Topics.DISPENSER_TOPIC, machineId);
        System.out.println(dispenserTopic);
        String jsonMessage = gson.toJson(s);
        mqttClient.publish(dispenserTopic + "/consumableAvailability", new MqttMessage(jsonMessage.getBytes()));
    }

    private void consumableAvailabilityResponseHandler(String s, MqttMessage mqttMessage) {
        String jsonMessage = new String(mqttMessage.getPayload());
        DrinkAvailabilityResult consumableAvailability = gson.fromJson(jsonMessage, DrinkAvailabilityResult.class);
        if (consumableAvailability.isAvailable()) {
            System.out.println("Drink available");
        } else {
            try {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC, new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Drink not available")).getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void newSelectionHandler(String topic, MqttMessage mqttMessage) {
        try {
            if (state != State.IDLE) {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC, new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Transaction in progress...")).getBytes()));
                return;
            }

            String jsonMessage = new String(mqttMessage.getPayload());
            Selection selection = gson.fromJson(jsonMessage, Selection.class);
            selectedBeverage = selection.getDrinkCode();
            checkConsumableAvailability(selection);

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    private void checkConsumableAvailability(Selection selectedBeverage) throws MqttException {
        String dispenserTopic = String.format(Topics.DISPENSER_TOPIC, machineId);
        String jsonMessage = gson.toJson(selectedBeverage);
        mqttClient.publish(dispenserTopic + "/consumableAvailability", new MqttMessage(jsonMessage.getBytes()));
    }

}
