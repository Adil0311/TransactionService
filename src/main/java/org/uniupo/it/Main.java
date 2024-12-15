package org.uniupo.it;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.uniupo.it.mqttConfig.MqttOptions;
import org.uniupo.it.service.TransactionService;

import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        String mqttUrl = "";
        String machineId = "";

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
            mqttUrl = properties.getProperty("mqttUrl");
            machineId = properties.getProperty("machineId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MqttClient mqttClient = new MqttClient(mqttUrl, UUID.randomUUID() + " " + machineId);
            MqttConnectOptions mqttOptions = new MqttOptions().getOptions();
            mqttClient.connect(mqttOptions);
           TransactionService transactionService = new TransactionService(machineId, mqttClient);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    }
