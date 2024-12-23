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
        this.mqttClient.subscribe(baseTopic + "/consumableAvailabilityResponse",
                                  this::consumableAvailabilityResponseHandler);
        this.mqttClient.subscribe(baseTopic + "/checkMachineStatusResponse", this::checkMachineStatusResponseHandler);
        this.mqttClient.subscribe(String.format(Topics.BALANCE_CHECK_TOPIC_RESPONSE, machineId),
                                  this::balanceResponseHandler);
        this.state = State.IDLE;
        this.selectedBeverage = "beverageCode";

        Selection selection = new Selection("1", 2); // codice 1 = espresso, 2 cucchiaini di zucchero
        String selectionJson = gson.toJson(selection);
        newSelectionHandler(baseTopic + "/newSelection", new MqttMessage(selectionJson.getBytes()));
    }

    private void balanceResponseHandler(String topic, MqttMessage mqttMessage) {
        String jsonMessage = new String(mqttMessage.getPayload());
        boolean isBalanceOk = gson.fromJson(jsonMessage, Boolean.class);
        if (isBalanceOk) {
            System.out.println("Balance ok");
        } else {
            try {
                System.out.println("Balance not ok");
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC_UPDATE,
                                   new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Insufficient balance")).getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private void checkMachineStatusResponseHandler(String topic, MqttMessage mqttMessage) {
        String jsonMessage = new String(mqttMessage.getPayload());
        System.out.println(jsonMessage);
        System.out.println(topic);
        boolean isFault = gson.fromJson(jsonMessage, Boolean.class);
        if (isFault) {
            try {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC_UPDATE,
                                   new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Machine fault")).getBytes()));
                System.out.println("Machine fault");
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                checkConsumableAvailability(new Selection(selectedBeverage, 5));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void consumableAvailabilityResponseHandler(String s, MqttMessage mqttMessage) {
        String jsonMessage = new String(mqttMessage.getPayload());
        DrinkAvailabilityResult consumableAvailability = gson.fromJson(jsonMessage, DrinkAvailabilityResult.class);
        System.out.println(consumableAvailability.toString());

        if (consumableAvailability.isAvailable()) {
            System.out.println("Drink available");
            try {
                mqttClient.publish(String.format(Topics.BALANCE_CHECK_TOPIC, machineId),
                                   new MqttMessage(selectedBeverage.getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC_UPDATE,
                                   new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Drink not available")).getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void newSelectionHandler(String topic, MqttMessage mqttMessage) {
        try {
            if (state != State.IDLE) {
                mqttClient.publish(baseTopic + Topics.DISPLAY_TOPIC_UPDATE,
                                   new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Transaction in " +
                                           "progress...")).getBytes()));
                return;
            }

            String jsonMessage = new String(mqttMessage.getPayload());
            Selection selection = gson.fromJson(jsonMessage, Selection.class);
            selectedBeverage = selection.getDrinkCode();
            checkMachineState();

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkMachineState() {
        try {
            System.out.println("Checking machine state on topic: " + String.format(Topics.ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC, machineId));
            mqttClient.publish(String.format(Topics.ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC, machineId),
                               new MqttMessage("".getBytes()));

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    private void checkConsumableAvailability(Selection selectedBeverage) throws MqttException {
        System.out.println("Checking consumable availability");
        String dispenserTopic = String.format(Topics.DISPENSER_TOPIC, machineId);
        String jsonMessage = gson.toJson(selectedBeverage);
        mqttClient.publish(dispenserTopic + "/consumableAvailability", new MqttMessage(jsonMessage.getBytes()));
    }

}
