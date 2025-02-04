package org.uniupo.it.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.dao.DrinkDao;
import org.uniupo.it.dao.DrinkDaoImpl;
import org.uniupo.it.dao.TransactionDao;
import org.uniupo.it.dao.TransactionDaoImpl;
import org.uniupo.it.model.*;
import org.uniupo.it.util.Topics;

public class TransactionService {
    final private String machineId;
    final private String instituteId;
    final private MqttClient mqttClient;
    final private Gson gson;
    private Selection selection;
    private State state;


    public TransactionService(String instituteId, String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.instituteId = instituteId;
        this.gson = new Gson();
        this.state = State.IDLE;
        subscribeToTopics();
    }

    private void handleError(String errorMessage) {
        try {

            DisplayMessageFormat message = new DisplayMessageFormat(true, errorMessage);
            mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(message).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Failed to publish error message", e);
        }
    }

    private void subscribeToTopics() throws MqttException {
        mqttClient.subscribe(String.format(Topics.TRANSACTION_NEW_SELECTION_TOPIC, instituteId, machineId), this::newSelectionHandler);
        mqttClient.subscribe(String.format(Topics.TRANSACTION_CONSUMABLE_AVAILABILITY_TOPIC_RESPONSE, instituteId, machineId), this::consumableAvailabilityResponseHandler);
        mqttClient.subscribe(String.format(Topics.RESPONSE_ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC, instituteId, machineId), this::checkMachineStatusResponseHandler);
        mqttClient.subscribe(String.format(Topics.BALANCE_CHECK_TOPIC_RESPONSE, instituteId, machineId), this::balanceResponseHandler);
        mqttClient.subscribe(String.format(Topics.DISPENSE_COMPLETED_TOPIC, instituteId, machineId), this::dispenseCompletedHandler);
        mqttClient.subscribe(String.format((Topics.TRANSACTION_NEW_COIN_INSERTED_TOPIC), instituteId, machineId), this::newCoinInsertedHandler);
        mqttClient.subscribe(String.format(Topics.TRANSACTION_CANCEL_SELECTION_TOPIC, instituteId, machineId), this::cancelSelectionHandler);
        mqttClient.subscribe(String.format(Topics.KILL_SERVICE_TOPIC, instituteId, machineId), this::killServiceHandler);
    }

    private void killServiceHandler(String topic, MqttMessage message) {
        System.out.println("Service killed hello darkness my old friend :(");
        new Thread(()->{
            try {
                Thread.sleep(1000);
                if(mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error during shutdown: "+e.getMessage());
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }

    private void cancelSelectionHandler(String topic, MqttMessage message) {
        if (state == State.DISPENSING) {
            handleError("Dispensing in progress");
            return;
        }
        this.state = State.IDLE;
        try {
            mqttClient.publish(String.format(Topics.BALANCE_RETURN_MONEY_TOPIC, instituteId, machineId), new MqttMessage("Return Money".getBytes()));
            mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(new DisplayMessageFormat(false, "Acquisto eliminato")).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void newCoinInsertedHandler(String topic, MqttMessage message) {
        if (state == State.DISPENSING) {
            handleError("Dispensing in progress");
            return;
        }
        if (state == State.IN_PAYMENT) {
            checkCreditAndDispense();
        }
    }

    private void dispenseCompletedHandler(String s, MqttMessage message) {
        System.out.println("Dispense completed handler called");
        TransactionMessage transactionMessage = getTransactionMessage();
        System.out.println("Transaction message: " + transactionMessage);
        this.state = State.IDLE;
        try {
            mqttClient.publish(Topics.REGISTER_TRANSACTION_TOPIC, new MqttMessage(gson.toJson(transactionMessage, TransactionMessage.class).getBytes()));
            System.out.println("Transaction registered");
            mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(new DisplayMessageFormat(false, "Acquisto completato!")).getBytes()));
        } catch (MqttException e) {
            System.out.println("Error in dispense completed handler"+e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private TransactionMessage getTransactionMessage() {
        TransactionDao transactionDao = new TransactionDaoImpl(instituteId,machineId);
        Transaction transaction = new Transaction(selection.getDrinkCode(), selection.getSugarLevel());
        Transaction createdTransaction = transactionDao.registerTransaction(transaction);
        return new TransactionMessage(machineId,
                instituteId,
                createdTransaction.getDrinkCode(),
                createdTransaction.getSugarQuantity(),
                createdTransaction.getTransactionId(),
                createdTransaction.getTimestamp());
    }

    private void balanceResponseHandler(String topic, MqttMessage mqttMessage) {
        String jsonMessage = new String(mqttMessage.getPayload());
        boolean isBalanceOk = gson.fromJson(jsonMessage, Boolean.class);
        if (isBalanceOk) {
            System.out.println("Balance ok");
            this.state = State.IN_PAYMENT;
            checkCreditAndDispense();
        } else {
            try {
                System.out.println("Balance not ok");
                mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Cassa piena")).getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void checkCreditAndDispense() {
        DrinkDao drinkDao = new DrinkDaoImpl(instituteId,machineId);
        TransactionDao transactionDao = new TransactionDaoImpl(instituteId,machineId);
        double drinkPrice = drinkDao.getPriceByCode(this.selection.getDrinkCode());
        double currentCredit = transactionDao.getCurrentCredit();

        if (currentCredit >= drinkPrice) {
            try {
                Selection selection = new Selection(this.selection.getDrinkCode(), this.selection.getSugarLevel());
                String jsonSelection = gson.toJson(selection);
                mqttClient.publish(String.format(Topics.DISPENSER_TOPIC_DISPENSE, instituteId, machineId), new MqttMessage(jsonSelection.getBytes()));
                System.out.println("Dispensing on " + String.format(Topics.DISPENSER_TOPIC_DISPENSE, instituteId, machineId));
                this.state = State.DISPENSING;
                System.out.println("Dispensing");
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Credit not enough");
            handleError("Credit not enough");
        }
    }


    private void checkMachineStatusResponseHandler(String topic, MqttMessage mqttMessage){
        System.out.println("Check machine status response handler called"+topic);
        String jsonMessage = new String(mqttMessage.getPayload());
        System.out.println("Json message: "+jsonMessage);

        try {
            boolean isFault = gson.fromJson(jsonMessage, Boolean.class);
            System.out.println("Machine fault: " + isFault);
            if (isFault) {
                try {
                    mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Il distributore è guasto")).getBytes()));
                    System.out.println("Machine fault");
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    checkConsumableAvailability(this.selection);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    private void consumableAvailabilityResponseHandler(String s, MqttMessage mqttMessage) {
        System.out.println("Consumable availability response handler called");
        String jsonMessage = new String(mqttMessage.getPayload());
        DrinkAvailabilityResult consumableAvailability = gson.fromJson(jsonMessage, DrinkAvailabilityResult.class);
        System.out.println(consumableAvailability.toString());

        if (consumableAvailability.isAvailable()) {
            System.out.println("Drink available");
            try {
                mqttClient.publish(String.format(Topics.BALANCE_CHECK_TOPIC, instituteId, machineId), new MqttMessage(this.selection.getDrinkCode().getBytes()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        } else {
            handleError("Drink not available");
        }
    }

    private void newSelectionHandler(String topic, MqttMessage mqttMessage) {
        System.out.println("Handler called for topic: " + topic);
        try {
            if (state != State.IDLE) {
                mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(new DisplayMessageFormat(true, "Acquisto in corso...")).getBytes()));
                return;
            }

            String jsonMessage = new String(mqttMessage.getPayload());
            System.out.println("New selection message: " + jsonMessage);
            this.selection = gson.fromJson(jsonMessage, Selection.class);
            System.out.println("New selection: " + selection.toString());
            checkMachineState();

        } catch (MqttException e) {
            System.out.println("Error in new selection handler"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void checkMachineState() {
        try {
            mqttClient.publish(String.format(Topics.ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC, instituteId, machineId), new MqttMessage("".getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    private void checkConsumableAvailability(Selection selectedBeverage) throws MqttException {
        System.out.println("Checking consumable availability");
        String dispenserTopic = String.format(Topics.DISPENSER_TOPIC, instituteId, machineId);
        String jsonMessage = gson.toJson(selectedBeverage);
        mqttClient.publish(dispenserTopic + "/consumableAvailability", new MqttMessage(jsonMessage.getBytes()));
    }

}
