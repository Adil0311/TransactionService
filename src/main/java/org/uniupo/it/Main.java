package org.uniupo.it;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.uniupo.it.mqttConfig.MqttOptions;
import org.uniupo.it.service.TransactionService;

import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        String mqttUrl = "ssl://localhost:8883";
        String machineId = "";
        String instituteId = "";

        if (args.length != 2) {
            System.out.println("Parametri non validi");
            System.exit(1);
        }

        instituteId = args[0];
        machineId = args[1];



        try {
            MqttClient mqttClient = new MqttClient(mqttUrl, UUID.randomUUID() + " " + machineId);
            MqttConnectOptions mqttOptions = new MqttOptions().getOptions();
            mqttClient.connect(mqttOptions);
            TransactionService transactionService = new TransactionService(instituteId,machineId, mqttClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
